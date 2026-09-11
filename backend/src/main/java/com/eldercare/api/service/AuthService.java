package com.eldercare.api.service;

import com.eldercare.core.dto.LoginRequest;
import com.eldercare.core.dto.RegisterRequest;
import com.eldercare.core.dto.ResetPasswordRequest;
import com.eldercare.core.entity.RefreshToken;
import com.eldercare.core.entity.SmsCode;
import com.eldercare.core.entity.User;
import com.eldercare.core.enums.MemberLevelEnum;
import com.eldercare.core.enums.RoleEnum;
import com.eldercare.core.enums.UserStatusEnum;
import com.eldercare.core.exception.AuthenticationException;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.RefreshTokenMapper;
import com.eldercare.core.mapper.SmsCodeMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.SmsService;
import com.eldercare.core.util.JwtUtil;
import com.eldercare.core.vo.LoginVO;
import com.eldercare.core.vo.TokenVO;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 【用户端】认证授权服务（/api/auth 公开接口）：
 * <ul>
 *   <li>短信验证码：Redis + Lua 滑动窗口限流（3 次/分钟、10 次/天），验证码 5 分钟有效</li>
 *   <li>注册：验证码校验 + 手机号唯一 + BCrypt 加密，初始 role=MEMBER / points=100 / NORMAL</li>
 *   <li>登录：JWT 双 Token，密码连续错误 5 次锁定 30 分钟</li>
 *   <li>刷新：校验 Refresh Token 签名 + 库记录 + 黑名单，签发新 Access Token</li>
 *   <li>登出：Access/Refresh Token 加入 Redis 黑名单，物理删除 Refresh Token 记录</li>
 *   <li>找回密码：验证码校验 + BCrypt 重置 + 强制全部设备下线（用户级黑名单）</li>
 * </ul>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Token 黑名单（与 JwtAuthenticationFilter 中 key 保持一致） */
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String USER_BLACKLIST_PREFIX = "jwt:blacklist:user:";

    /** 短信限流：每分钟 3 次（滑动窗口）、每天 10 次 */
    private static final String SMS_MINUTE_KEY = "rl:sms:min:";
    private static final String SMS_DAY_KEY = "rl:sms:day:";
    private static final int SMS_MAX_PER_MINUTE = 3;
    private static final int SMS_MAX_PER_DAY = 10;
    private static final int SMS_CODE_TTL_MINUTES = 5;

    /** 登录失败锁定：连续 5 次错误锁定 30 分钟 */
    private static final String LOGIN_ERR_KEY = "rl:login:err:";
    private static final String LOGIN_LOCK_KEY = "rl:login:lock:";
    private static final int LOGIN_MAX_ERRORS = 5;
    private static final int LOGIN_LOCK_MINUTES = 30;

    /** 注册奖励积分 */
    private static final int REGISTER_BONUS_POINTS = 100;

    /** 短信每分钟限流 Lua 脚本（滑动窗口）：KEYS[1]=zset, ARGV[1]=now, ARGV[2]=windowStart, ARGV[3]=max, ARGV[4]=windowSeconds */
    private static final String SMS_MINUTE_LUA =
            "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[2]) " +
            "local count = redis.call('ZCARD', KEYS[1]) " +
            "if count >= tonumber(ARGV[3]) then return 0 end " +
            "redis.call('ZADD', KEYS[1], ARGV[1], ARGV[1]) " +
            "redis.call('EXPIRE', KEYS[1], ARGV[4]) " +
            "return 1";
    private static final DefaultRedisScript<Long> SMS_MINUTE_SCRIPT =
            new DefaultRedisScript<>(SMS_MINUTE_LUA, Long.class);

    /** 短信每天限流 Lua 脚本（固定窗口）：KEYS[1]=counter, ARGV[1]=max, ARGV[2]=expireSeconds */
    private static final String SMS_DAY_LUA =
            "local count = redis.call('INCR', KEYS[1]) " +
            "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[2]) end " +
            "if count > tonumber(ARGV[1]) then return 0 end " +
            "return 1";
    private static final DefaultRedisScript<Long> SMS_DAY_SCRIPT =
            new DefaultRedisScript<>(SMS_DAY_LUA, Long.class);

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final SmsCodeMapper smsCodeMapper;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final PointService pointService;

    public AuthService(UserMapper userMapper, RefreshTokenMapper refreshTokenMapper,
                       SmsCodeMapper smsCodeMapper, SmsService smsService,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       StringRedisTemplate stringRedisTemplate, PointService pointService) {
        this.userMapper = userMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.smsCodeMapper = smsCodeMapper;
        this.smsService = smsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
        this.pointService = pointService;
    }

    // ==================== 发送验证码 ====================

    public void sendSmsCode(String phone) {
        checkSmsMinuteLimit(phone);
        checkSmsDayLimit(phone);

        // 生成 6 位验证码并入库（5 分钟有效）
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setExpireTime(LocalDateTime.now().plusMinutes(SMS_CODE_TTL_MINUTES));
        smsCodeMapper.insert(smsCode);

        try {
            smsService.sendVerifyCode(phone, code);
            log.info("验证码已发送: phone={}", phone);
        } catch (Exception e) {
            log.error("短信发送失败: phone={}", phone, e);
            throw new BusinessException(500, "验证码发送失败，请稍后重试");
        }
    }

    // ==================== 注册 ====================

    @Transactional
    public void register(RegisterRequest req) {
        if (userMapper.selectByPhone(req.getPhone()) != null) {
            throw new BusinessException(409, "该手机号已注册，请直接登录");
        }
        SmsCode smsCode = verifySmsCode(req.getPhone(), req.getCode());

        User user = new User();
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(RoleEnum.MEMBER.name());
        user.setMemberLevel(MemberLevelEnum.NORMAL.name());
        // 生产环境由 PointService 写入积分流水；保留空依赖兼容纯认证单测构造场景。
        user.setPoints(pointService == null ? REGISTER_BONUS_POINTS : 0);
        user.setStatus(UserStatusEnum.ENABLED.name());
        userMapper.insert(user);
        if (pointService != null) {
            pointService.grant(user.getId(), REGISTER_BONUS_POINTS, "REGISTER", String.valueOf(user.getId()), "注册奖励");
        }

        // 标记验证码已使用（used 等价逻辑删除）
        smsCodeMapper.updateUsed(smsCode.getId());
        log.info("用户注册成功: id={}, phone={}", user.getId(), req.getPhone());
    }

    // ==================== 登录 ====================

    public LoginVO login(LoginRequest req) {
        checkLoginLock(req.getPhone());

        User user = userMapper.selectByPhone(req.getPhone());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            recordLoginError(req.getPhone());
            throw new BusinessException(400, "手机号或密码错误");
        }
        if (UserStatusEnum.DISABLED.name().equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        // 登录成功，清除失败计数与锁定
        clearLoginError(req.getPhone());
        // 若此前发生过重置密码/强制下线，重新登录即视为新会话，清除用户级黑名单，否则新 Token 也会被拒
        stringRedisTemplate.delete(USER_BLACKLIST_PREFIX + user.getId());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        saveRefreshToken(user.getId(), refreshToken);

        log.info("用户登录成功: id={}, phone={}", user.getId(), req.getPhone());
        return buildLoginVO(user, accessToken, refreshToken);
    }

    // ==================== 刷新 Access Token ====================

    public TokenVO refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(400, "refreshToken 不能为空");
        }
        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (Exception e) {
            throw new AuthenticationException("刷新令牌无效或已过期");
        }
        if (!JwtUtil.TYPE_REFRESH.equals(jwtUtil.getTokenType(claims))) {
            throw new AuthenticationException("令牌类型不正确");
        }

        Long userId = jwtUtil.getUserId(claims);
        String jti = jwtUtil.getJti(claims);
        // 强制下线 / 登出后失效检查
        if (isUserBlacklisted(userId) || isTokenBlacklisted(jti)) {
            throw new AuthenticationException("登录状态已失效，请重新登录");
        }
        // 库记录必须存在且未过期
        RefreshToken rt = refreshTokenMapper.selectByToken(refreshToken);
        if (rt == null || rt.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("刷新令牌已失效，请重新登录");
        }
        // 用户仍存在且可用
        User user = userMapper.selectById(userId);
        if (user == null || UserStatusEnum.DISABLED.name().equals(user.getStatus())) {
            throw new AuthenticationException("账号不存在或已被禁用");
        }

        TokenVO vo = new TokenVO();
        vo.setAccessToken(jwtUtil.generateAccessToken(userId, user.getRole()));
        vo.setTokenType("Bearer");
        vo.setExpiresIn((int) jwtUtil.getAccessTokenExpire());
        log.info("刷新 Access Token 成功: userId={}", userId);
        return vo;
    }

    // ==================== 登出 ====================

    public void logout(String accessToken, String refreshToken) {
        if (!StringUtils.hasText(accessToken) && !StringUtils.hasText(refreshToken)) {
            throw new BusinessException(400, "缺少令牌");
        }
        // Access Token 加入黑名单（TTL=剩余有效期）
        if (StringUtils.hasText(accessToken)) {
            blacklistToken(accessToken);
        }
        // 物理删除 Refresh Token 记录并加入黑名单
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenMapper.deleteByToken(refreshToken);
            blacklistToken(refreshToken);
        }
        log.info("用户登出完成: userId={}", accessToken != null ? parseUserIdQuietly(accessToken) : "unknown");
    }

    // ==================== 找回密码 ====================

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        SmsCode smsCode = verifySmsCode(req.getPhone(), req.getCode());

        User user = userMapper.selectByPhone(req.getPhone());
        if (user == null) {
            throw new ResourceNotFoundException("该手机号未注册");
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(req.getNewPassword()));
        smsCodeMapper.updateUsed(smsCode.getId());

        // 删除所有 Refresh Token，并将用户加入黑名单，强制所有设备下线
        refreshTokenMapper.deleteByUserId(user.getId());
        stringRedisTemplate.opsForValue().set(USER_BLACKLIST_PREFIX + user.getId(), "1",
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);
        log.info("密码重置成功: userId={}, phone={}", user.getId(), req.getPhone());
    }

    // ==================== 私有方法 ====================

    /** 短信每分钟限流：Redis + Lua 滑动窗口（3 次/分钟） */
    private void checkSmsMinuteLimit(String phone) {
        long now = System.currentTimeMillis();
        Long allowed = stringRedisTemplate.execute(SMS_MINUTE_SCRIPT, List.of(SMS_MINUTE_KEY + phone),
                String.valueOf(now), String.valueOf(now - 60_000L),
                String.valueOf(SMS_MAX_PER_MINUTE), "60");
        if (allowed != null && allowed == 0L) {
            throw new BusinessException(429, "发送过于频繁，请 1 分钟后再试");
        }
    }

    /** 短信每天限流：固定窗口（10 次/天） */
    private void checkSmsDayLimit(String phone) {
        Long allowed = stringRedisTemplate.execute(SMS_DAY_SCRIPT, List.of(SMS_DAY_KEY + phone),
                String.valueOf(SMS_MAX_PER_DAY), "86400");
        if (allowed != null && allowed == 0L) {
            throw new BusinessException(429, "今日验证码发送次数已达上限");
        }
    }

    /** 校验验证码：最新一条未使用、不匹配或过期均拒绝 */
    private SmsCode verifySmsCode(String phone, String code) {
        SmsCode smsCode = smsCodeMapper.selectLatestByPhone(phone);
        if (smsCode == null || !code.equals(smsCode.getCode())) {
            throw new BusinessException("验证码错误");
        }
        if (smsCode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        return smsCode;
    }

    /** 密码错误计数，连续 5 次后锁定 30 分钟 */
    private void recordLoginError(String phone) {
        Long count = stringRedisTemplate.opsForValue().increment(LOGIN_ERR_KEY + phone);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(LOGIN_ERR_KEY + phone, LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
        }
        if (count != null && count >= LOGIN_MAX_ERRORS) {
            stringRedisTemplate.opsForValue().set(LOGIN_LOCK_KEY + phone, "1", LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
            stringRedisTemplate.delete(LOGIN_ERR_KEY + phone);
            throw new BusinessException(429, "密码错误次数过多，账号已锁定 30 分钟");
        }
    }

    private void checkLoginLock(String phone) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOGIN_LOCK_KEY + phone))) {
            throw new BusinessException(429, "账号已锁定，请 30 分钟后再试");
        }
    }

    private void clearLoginError(String phone) {
        stringRedisTemplate.delete(LOGIN_ERR_KEY + phone);
        stringRedisTemplate.delete(LOGIN_LOCK_KEY + phone);
    }

    /** 保存 Refresh Token 到库 */
    private void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setToken(refreshToken);
        rt.setExpireTime(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpire()));
        refreshTokenMapper.insert(rt);
    }

    /** 将 Token 加入黑名单，TTL=剩余有效期 */
    private void blacklistToken(String token) {
        try {
            Claims claims = jwtUtil.parseToken(token);
            String jti = jwtUtil.getJti(claims);
            long ttl = jwtUtil.getRemainingExpiration(claims);
            if (ttl > 0) {
                stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttl, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.debug("登出时令牌解析失败（可能已过期）: {}", e.getMessage());
        }
    }

    private boolean isTokenBlacklisted(String jti) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }

    private boolean isUserBlacklisted(Long userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(USER_BLACKLIST_PREFIX + userId));
    }

    private LoginVO buildLoginVO(User user, String accessToken, String refreshToken) {
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setMemberLevel(user.getMemberLevel());
        vo.setPoints(user.getPoints());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    /** 仅用于日志：静默解析 token 中的 userId，失败返回 null */
    private Long parseUserIdQuietly(String token) {
        try {
            return jwtUtil.getUserId(jwtUtil.parseToken(token));
        } catch (Exception e) {
            return null;
        }
    }
}

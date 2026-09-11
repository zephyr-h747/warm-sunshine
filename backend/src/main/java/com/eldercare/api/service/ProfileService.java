package com.eldercare.api.service;

import com.eldercare.core.dto.ChangePasswordRequest;
import com.eldercare.core.dto.UpdateProfileRequest;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.UserProfileVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 【用户端】个人中心服务
 */
@Service
public class ProfileService {

    /** 用户级黑名单前缀（与 AuthService/JwtAuthenticationFilter 一致） */
    private static final String USER_BLACKLIST_PREFIX = "jwt:blacklist:user:";
    /** 强制下线后黑名单保留时长（7 天，重新登录时清除） */
    private static final long USER_BLACKLIST_TTL_SECONDS = 7 * 24 * 3600L;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    public ProfileService(UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 获取个人信息 */
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        return toProfileVO(user);
    }

    /** 更新个人信息 */
    public UserProfileVO updateProfile(Long userId, UpdateProfileRequest req) {
        User update = new User();
        update.setId(userId);
        update.setRealName(req.getRealName());
        update.setGender(req.getGender());
        update.setBirthDate(req.getBirthDate());
        update.setHeight(req.getHeight());
        update.setAvatar(req.getAvatar());
        update.setEmergencyContact(req.getEmergencyContact());
        userMapper.updateProfile(update);
        return getProfile(userId);
    }

    /**
     * 修改密码：校验旧密码 → BCrypt 加密新密码 → 更新 → 强制下线（用户级黑名单，所有旧 Token 失效）
     */
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码不正确");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new BusinessException(400, "新密码不能与旧密码相同");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(req.getNewPassword()));
        // 强制下线：此后所有携带旧 Token 的请求都会被 JWT 过滤器拒绝
        stringRedisTemplate.opsForValue().set(USER_BLACKLIST_PREFIX + userId, "1",
                USER_BLACKLIST_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private UserProfileVO toProfileVO(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setGender(user.getGender());
        vo.setBirthDate(user.getBirthDate());
        vo.setHeight(user.getHeight());
        vo.setAvatar(user.getAvatar());
        vo.setEmergencyContact(user.getEmergencyContact());
        vo.setMemberLevel(user.getMemberLevel());
        vo.setPoints(user.getPoints());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}

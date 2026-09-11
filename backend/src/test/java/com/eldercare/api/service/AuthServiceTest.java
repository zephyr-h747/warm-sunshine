package com.eldercare.api.service;

import com.eldercare.core.dto.LoginRequest;
import com.eldercare.core.dto.RegisterRequest;
import com.eldercare.core.entity.RefreshToken;
import com.eldercare.core.entity.SmsCode;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.mapper.RefreshTokenMapper;
import com.eldercare.core.mapper.SmsCodeMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.SmsService;
import com.eldercare.core.util.JwtUtil;
import com.eldercare.core.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthService 核心路径单元测试：注册、登录
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    private static final String PHONE = "13800138000";

    @Mock
    private UserMapper userMapper;
    @Mock
    private RefreshTokenMapper refreshTokenMapper;
    @Mock
    private SmsCodeMapper smsCodeMapper;
    @Mock
    private SmsService smsService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(jwtUtil.getAccessTokenExpire()).thenReturn(7200L);
        when(jwtUtil.getRefreshTokenExpire()).thenReturn(604800L);
    }

    private RegisterRequest buildRegisterRequest(String code) {
        RegisterRequest req = new RegisterRequest();
        req.setPhone(PHONE);
        req.setCode(code);
        req.setPassword("Test@123456");
        return req;
    }

    private SmsCode buildValidSmsCode(String code) {
        SmsCode smsCode = new SmsCode();
        smsCode.setId(1L);
        smsCode.setPhone(PHONE);
        smsCode.setCode(code);
        smsCode.setExpireTime(LocalDateTime.now().plusMinutes(5));
        return smsCode;
    }

    // ==================== 注册 ====================

    @Test
    void register_shouldCreateMemberWithBonusPointsAndMarkCodeUsed() {
        when(userMapper.selectByPhone(PHONE)).thenReturn(null);
        when(smsCodeMapper.selectLatestByPhone(PHONE)).thenReturn(buildValidSmsCode("123456"));
        when(passwordEncoder.encode("Test@123456")).thenReturn("$2a$10$mockEncoded");

        authService.register(buildRegisterRequest("123456"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User created = captor.getValue();
        assertEquals(PHONE, created.getPhone());
        assertEquals("MEMBER", created.getRole());
        assertEquals("NORMAL", created.getMemberLevel());
        assertEquals(Integer.valueOf(100), created.getPoints());
        assertEquals("ENABLED", created.getStatus());
        assertEquals("$2a$10$mockEncoded", created.getPassword());
        verify(smsCodeMapper).updateUsed(1L);
    }

    @Test
    void register_shouldRejectWhenPhoneAlreadyRegistered() {
        when(userMapper.selectByPhone(PHONE)).thenReturn(new User());

        assertThrows(BusinessException.class, () -> authService.register(buildRegisterRequest("123456")));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void register_shouldRejectWhenCodeMismatch() {
        when(userMapper.selectByPhone(PHONE)).thenReturn(null);
        when(smsCodeMapper.selectLatestByPhone(PHONE)).thenReturn(buildValidSmsCode("123456"));

        assertThrows(BusinessException.class, () -> authService.register(buildRegisterRequest("000000")));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void register_shouldRejectWhenCodeExpired() {
        when(userMapper.selectByPhone(PHONE)).thenReturn(null);
        SmsCode expired = buildValidSmsCode("123456");
        expired.setExpireTime(LocalDateTime.now().minusMinutes(1));
        when(smsCodeMapper.selectLatestByPhone(PHONE)).thenReturn(expired);

        assertThrows(BusinessException.class, () -> authService.register(buildRegisterRequest("123456")));
        verify(userMapper, never()).insert(any(User.class));
    }

    // ==================== 登录 ====================

    @Test
    void login_shouldReturnDualTokensAndPersistRefreshToken() {
        User user = new User();
        user.setId(1L);
        user.setPhone(PHONE);
        user.setPassword("$2a$10$stored");
        user.setRole("MEMBER");
        user.setMemberLevel("NORMAL");
        user.setPoints(100);
        user.setStatus("ENABLED");

        when(userMapper.selectByPhone(PHONE)).thenReturn(user);
        when(passwordEncoder.matches("Test@123456", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(1L, "MEMBER")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(1L)).thenReturn("refresh-token");

        LoginRequest req = new LoginRequest();
        req.setPhone(PHONE);
        req.setPassword("Test@123456");

        LoginVO vo = authService.login(req);

        assertNotNull(vo);
        assertEquals("access-token", vo.getAccessToken());
        assertEquals("refresh-token", vo.getRefreshToken());
        assertEquals(Long.valueOf(1L), vo.getUserId());
        assertEquals("MEMBER", vo.getRole());
        assertEquals(Integer.valueOf(100), vo.getPoints());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals("refresh-token", captor.getValue().getToken());
    }

    @Test
    void login_shouldClearUserBlacklistAfterResetPassword() {
        User user = new User();
        user.setId(1L);
        user.setPhone(PHONE);
        user.setPassword("$2a$10$stored");
        user.setRole("MEMBER");
        user.setStatus("ENABLED");

        when(userMapper.selectByPhone(PHONE)).thenReturn(user);
        when(passwordEncoder.matches("Test@123456", user.getPassword())).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setPhone(PHONE);
        req.setPassword("Test@123456");
        authService.login(req);

        // 重新登录成功应清除 resetPassword 设置的用户级黑名单，否则新 Token 无法使用
        verify(stringRedisTemplate).delete("jwt:blacklist:user:1");
    }

    @Test
    void login_shouldRejectWhenPasswordWrongAndNotIssueToken() {
        User user = new User();
        user.setId(1L);
        user.setPhone(PHONE);
        user.setPassword("$2a$10$stored");
        user.setRole("MEMBER");
        user.setStatus("ENABLED");

        when(userMapper.selectByPhone(PHONE)).thenReturn(user);
        when(passwordEncoder.matches("Wrong@123", user.getPassword())).thenReturn(false);
        when(valueOps.increment("rl:login:err:" + PHONE)).thenReturn(1L);

        LoginRequest req = new LoginRequest();
        req.setPhone(PHONE);
        req.setPassword("Wrong@123");

        assertThrows(BusinessException.class, () -> authService.login(req));
        verify(refreshTokenMapper, never()).insert(any(RefreshToken.class));
    }

    @Test
    void login_shouldRejectWhenUserDisabled() {
        User user = new User();
        user.setId(1L);
        user.setPhone(PHONE);
        user.setPassword("$2a$10$stored");
        user.setRole("MEMBER");
        user.setStatus("DISABLED");

        when(userMapper.selectByPhone(PHONE)).thenReturn(user);
        when(passwordEncoder.matches("Test@123456", user.getPassword())).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setPhone(PHONE);
        req.setPassword("Test@123456");

        assertThrows(BusinessException.class, () -> authService.login(req));
        verify(refreshTokenMapper, never()).insert(any(RefreshToken.class));
    }

    @Test
    void login_shouldRejectWhenLocked() {
        when(stringRedisTemplate.hasKey("rl:login:lock:" + PHONE)).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setPhone(PHONE);
        req.setPassword("Test@123456");

        assertThrows(BusinessException.class, () -> authService.login(req));
        verify(userMapper, never()).selectByPhone(any());
    }
}

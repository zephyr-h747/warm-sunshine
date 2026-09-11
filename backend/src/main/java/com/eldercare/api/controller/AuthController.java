package com.eldercare.api.controller;

import com.eldercare.api.service.AuthService;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.LoginRequest;
import com.eldercare.core.dto.RefreshRequest;
import com.eldercare.core.dto.RegisterRequest;
import com.eldercare.core.dto.ResetPasswordRequest;
import com.eldercare.core.dto.SendCodeRequest;
import com.eldercare.core.vo.LoginVO;
import com.eldercare.core.vo.TokenVO;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【用户端】认证接口（/api/auth，公开访问，无需登录）：
 * 发送验证码 / 注册 / 登录 / 刷新 Token / 登出 / 找回密码
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 发送短信验证码 */
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SendCodeRequest req) {
        authService.sendSmsCode(req.getPhone());
        return Result.success("验证码已发送", null);
    }

    /** 注册（手机号 + 验证码 + 密码） */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return Result.success("注册成功", null);
    }

    /** 登录（手机号 + 密码） */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    /** 刷新 Access Token */
    @PostMapping("/refresh")
    public Result<TokenVO> refresh(@Valid @RequestBody RefreshRequest req) {
        return Result.success(authService.refresh(req.getRefreshToken()));
    }

    /** 登出（Access Token 取 Header，Refresh Token 取 Body，至少提供其一） */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                               @RequestBody(required = false) RefreshRequest req) {
        String accessToken = null;
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            accessToken = authorization.substring(BEARER_PREFIX.length()).trim();
        }
        String refreshToken = req != null ? req.getRefreshToken() : null;
        authService.logout(accessToken, refreshToken);
        return Result.success("退出登录成功", null);
    }

    /** 找回密码（手机号 + 验证码 + 新密码） */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return Result.success("密码重置成功", null);
    }
}

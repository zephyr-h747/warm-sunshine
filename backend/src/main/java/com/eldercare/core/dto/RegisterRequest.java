package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求（密码校验：长度≥8，含字母+数字）
 */
@Data
public class RegisterRequest {

    /** 手机号（登录账号） */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 短信验证码 */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在 8-32 位之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码需同时包含字母和数字")
    private String password;

}

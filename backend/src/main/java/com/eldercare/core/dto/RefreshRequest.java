package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求
 */
@Data
public class RefreshRequest {

    /** 刷新令牌（登录时下发的 refreshToken） */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;


}

package com.eldercare.core.exception;

import java.io.Serial;

/**
 * 认证异常（401）：未登录或 Token 无效/过期
 */
public class AuthenticationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

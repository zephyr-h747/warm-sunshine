package com.eldercare.core.exception;

import java.io.Serial;

/**
 * 资源不存在异常（404）
 */
public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

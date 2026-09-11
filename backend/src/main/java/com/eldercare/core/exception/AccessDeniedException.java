package com.eldercare.core.exception;

import java.io.Serial;

/**
 * 权限异常（403）：角色不足或资源非本人
 */
public class AccessDeniedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }
}

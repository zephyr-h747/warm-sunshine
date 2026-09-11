package com.eldercare.core.exception;

import java.io.Serial;

/**
 * 业务异常，携带业务码与提示信息
 */
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    /** 仅提示信息，业务码默认 400（参数或状态不合法） */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /** 自定义业务码（如 401 未登录、403 无权限、409 冲突） */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 携带原始异常，用于包装底层错误并保留堆栈 */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /** 返回业务码 */
    public int getCode() {
        return code;
    }
}

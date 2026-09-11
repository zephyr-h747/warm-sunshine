package com.eldercare.core.common;

import com.eldercare.core.exception.BusinessException;
import org.slf4j.MDC;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应封装
 * <p>
 * 字段：code(业务码)、message(提示信息)、data(数据)、traceId(链路追踪ID)
 *
 * @param <T> 数据类型
 */
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 成功业务码 */
    public static final int CODE_SUCCESS = 200;

    private int code;
    private String message;
    private T data;
    private String traceId;

    public Result() {
        this.traceId = MDC.get("traceId");
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = MDC.get("traceId");
    }

    public static <T> Result<T> success() {
        return new Result<>(CODE_SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(CODE_SUCCESS, message, data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(BusinessException e) {
        return new Result<>(e.getCode(), e.getMessage(), null);
    }

    public boolean isSuccess() {
        return this.code == CODE_SUCCESS;
    }

    // ---------- getter / setter ----------

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}

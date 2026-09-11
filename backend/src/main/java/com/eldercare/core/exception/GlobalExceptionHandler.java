package com.eldercare.core.exception;

import com.eldercare.core.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，统一返回 Result 结构
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} | path={}", e.getMessage(), request.getRequestURI());
        HttpStatus status = resolveHttpStatus(e.getCode());
        return ResponseEntity.status(status).body(Result.error(e.getCode(), e.getMessage()));
    }

    /** 认证异常 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证异常: {} | path={}", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, e.getMessage()));
    }

    /** 权限异常 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限异常: {} | path={}", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, e.getMessage()));
    }

    /** 资源不存在 */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("资源不存在: {} | path={}", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(404, e.getMessage()));
    }

    /** 参数校验异常（@RequestBody @Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {} | path={}", msg, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, msg));
    }

    /** 绑定异常（表单参数） */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {} | path={}", msg, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, msg));
    }

    /** 接口不存在（未知路径） */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResource(org.springframework.web.servlet.resource.NoResourceFoundException e, HttpServletRequest request) {
        log.warn("接口不存在: {} | path={}", e.getResourcePath(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(404, "接口不存在"));
    }

    /** Spring Security 认证异常（如未登录访问受保护接口） */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleSecAccessDenied(org.springframework.security.access.AccessDeniedException e, HttpServletRequest request) {
        log.warn("Security 权限拒绝: {} | path={}", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, "无权限访问"));
    }

    /** 文件上传超过大小限制（默认 20MB） */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传超限: {} | path={}", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, "文件大小不能超过 20MB"));
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} | path={}", e.getMessage(), request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error(500, "系统内部错误，请稍后重试"));
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    /**
     * 业务码映射 HTTP 状态码
     */
    private HttpStatus resolveHttpStatus(int code) {
        return switch (code) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}

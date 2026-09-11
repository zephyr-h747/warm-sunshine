package com.eldercare.core.util;

import com.eldercare.core.exception.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具：从 SecurityContext 获取当前登录用户 ID / 角色
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户 ID，未登录抛 401
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new AuthenticationException("未登录或登录已过期");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long id) {
            return id;
        }
        if (principal instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            throw new AuthenticationException("无法识别的用户身份");
        }
    }

    /**
     * 判断当前用户是否拥有指定角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /** 判断当前用户是否为管理员 */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}

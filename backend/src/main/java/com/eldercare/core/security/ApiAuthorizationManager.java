package com.eldercare.core.security;

import com.eldercare.core.mapper.PermissionMapper;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import java.util.function.Supplier;

/** 基于 user_role -> role_permission -> permission_resource -> resource 的接口权限判定。 */
@Component
public class ApiAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private static final String ADMIN_PATH_PREFIX = "/api/admin/";
    private static final String MEMBER_PATH_PREFIX = "/api/member/";
    private static final String ROLE_ADMIN_AUTHORITY = "ROLE_ADMIN";
    private static final String ROLE_MEMBER_AUTHORITY = "ROLE_MEMBER";
    private final PermissionMapper permissionMapper;
    private final AntPathMatcher matcher = new AntPathMatcher();
    public ApiAuthorizationManager(PermissionMapper permissionMapper) { this.permissionMapper = permissionMapper; }
    @Override public AuthorizationDecision authorize(Supplier<? extends Authentication> supplier, RequestAuthorizationContext context) {
        Authentication auth = supplier.get();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Number id)) return new AuthorizationDecision(false);
        String path = context.getRequest().getRequestURI(); String method = context.getRequest().getMethod();
        // 管理端接口：JWT 携带 ADMIN 角色即放行，不依赖权限种子数据（避免关联表缺数据时管理员被误拒）
        if (path.startsWith(ADMIN_PATH_PREFIX) && auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN_AUTHORITY.equals(a.getAuthority()))) {
            return new AuthorizationDecision(true);
        }
        // 会员端接口：JWT 携带 MEMBER 角色即放行（与 admin 同理，注册流程未写 user_role 关联时会员不被误拒）
        if (path.startsWith(MEMBER_PATH_PREFIX) && auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_MEMBER_AUTHORITY.equals(a.getAuthority()))) {
            return new AuthorizationDecision(true);
        }
        boolean granted = permissionMapper.selectApiResourcesByUserId(id.longValue()).stream()
                .anyMatch(r -> matcher.match(r.path(), path) && (r.method() == null || "*".equals(r.method()) || method.equalsIgnoreCase(r.method())));
        return new AuthorizationDecision(granted);
    }
}

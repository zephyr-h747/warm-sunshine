package com.eldercare.core.filter;

import com.eldercare.core.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：
 * 1. 从 Header 提取 Bearer Token
 * 2. 校验签名 + 黑名单（Redis 查 jwt:blacklist:{jti} 与用户级黑名单 jwt:blacklist:user:{userId}）
 * 3. 构建 Authentication 放入 SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String USER_BLACKLIST_PREFIX = "jwt:blacklist:user:";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, StringRedisTemplate stringRedisTemplate) {
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
            String token = header.substring(BEARER.length()).trim();
            try {
                Claims claims = jwtUtil.parseToken(token);
                String jti = jwtUtil.getJti(claims);
                Long userId = jwtUtil.getUserId(claims);
                String role = jwtUtil.getRole(claims);

                // 仅 Access Token 可用于接口访问
                if (!JwtUtil.TYPE_ACCESS.equals(jwtUtil.getTokenType(claims))) {
                    log.debug("非 Access Token，拒绝访问");
                } else if (userId != null && isBlacklisted(jti, userId)) {
                    log.debug("Token 已在黑名单: jti={}, userId={}", jti, userId);
                } else {
                    String authority = "ROLE_" + (role != null ? role : "MEMBER");
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, List.of(new SimpleGrantedAuthority(authority)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.debug("Token 解析失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 校验黑名单：单 Token 黑名单 + 用户级黑名单（强制下线所有设备）
     */
    private boolean isBlacklisted(String jti, Long userId) {
        Boolean exists = stringRedisTemplate.hasKey(BLACKLIST_PREFIX + jti);
        if (Boolean.TRUE.equals(exists)) {
            return true;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(USER_BLACKLIST_PREFIX + userId));
    }
}

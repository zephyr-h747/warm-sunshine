package com.eldercare.core.config;

import com.eldercare.core.filter.JwtAuthenticationFilter;
import com.eldercare.core.security.ApiAuthorizationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Spring Security 配置：
 * - 禁用 CSRF，无状态 session（JWT）
 * - 放行 /api/auth/**、/api/sms/**、actuator 健康检查
 * - /api/admin/** 需 ADMIN 角色，其余接口需认证
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiAuthorizationManager apiAuthorizationManager;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ApiAuthorizationManager apiAuthorizationManager) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiAuthorizationManager = apiAuthorizationManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // SseEmitter 完成/超时后容器的 ASYNC dispatch 会重新进入过滤链，此时
                        // SecurityContext 已清空，需放行（async 分发仅由容器在已通过初始鉴权的请求上触发）
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        // 公开接口
                        .requestMatchers("/api/auth/**", "/api/sms/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/error").permitAll()
                        // 报告下载链接自带 5 分钟签名，无需登录态（浏览器可直接打开）
                        .requestMatchers("/api/member/appointment/report/download").permitAll()
                        // 管理端接口需 ADMIN 角色
                        .requestMatchers("/api/admin/**", "/api/member/**").access(apiAuthorizationManager)
                        // 其余接口一律需要认证
                        .anyRequest().authenticated()
                )
                // 未认证/无权限时返回 JSON 而非默认 302/403 页面
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJson(response, 401, "未登录或登录已过期"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJson(response, 403, "无权限访问"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws java.io.IOException {
        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = new ObjectMapper().writeValueAsString(Map.of(
                "code", code,
                "message", message,
                "data", "",
                "traceId", ""
        ));
        response.getWriter().write(body);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

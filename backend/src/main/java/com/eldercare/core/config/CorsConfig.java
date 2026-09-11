package com.eldercare.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域配置：允许前端本地开发域名（会员端 5173、管理端 5174）
 * <p>
 * 前端 Vite 以 host 0.0.0.0 启动，支持局域网设备通过 http://<电脑IP>:517x 访问，
 * 因此按端口放行任意来源（开发环境）；生产环境由 Nginx 同源反向代理，不依赖此配置。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端来源（开发环境）：任意主机上的 5173/5174 端口，
        // 覆盖 localhost、127.0.0.1、局域网 IP（手机真机调试）等场景
        config.setAllowedOriginPatterns(List.of(
                "http://*:5173",
                "http://*:5174",
                "chrome-extension://*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        // 前端无感刷新需要读取该响应头
        config.addExposedHeader("Retry-After");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

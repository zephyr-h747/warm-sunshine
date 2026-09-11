package com.eldercare.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类：Access Token（2h）/ Refresh Token（7d）双 Token 生成与解析
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "ACCESS";
    public static final String TYPE_REFRESH = "REFRESH";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire:7200}")
    private long accessTokenExpire;

    @Value("${jwt.refresh-token-expire:604800}")
    private long refreshTokenExpire;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 要求密钥至少 256 位（32 字节）
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret 长度不足，HS256 要求至少 32 字节");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成 Access Token（2h，含 userId、role、jti） */
    public String generateAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpire * 1000L);
        return Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 生成 Refresh Token（7d，含 userId、jti） */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpire * 1000L);
        return Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 解析 Token，返回 Claims；无效抛出 JwtException */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验 Token 有效性（签名 + 未过期） */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /** 从 Claims 中取用户 ID */
    public Long getUserId(Claims claims) {
        Object val = claims.get(CLAIM_USER_ID);
        if (val instanceof Number n) {
            return n.longValue();
        }
        return val != null ? Long.parseLong(val.toString()) : null;
    }

    /** 从 Claims 中取角色 */
    public String getRole(Claims claims) {
        Object val = claims.get(CLAIM_ROLE);
        return val != null ? val.toString() : null;
    }

    /** 从 Claims 中取 jti（Token 唯一标识，黑名单用） */
    public String getJti(Claims claims) {
        return claims.getId();
    }

    /** 从 Claims 中取 Token 类型（ACCESS/REFRESH） */
    public String getTokenType(Claims claims) {
        Object val = claims.get(CLAIM_TYPE);
        return val != null ? val.toString() : null;
    }
    /** Access Token 有效期（秒） */
    public long getAccessTokenExpire() {
        return accessTokenExpire;
    }

    /** Refresh Token 有效期（秒） */
    public long getRefreshTokenExpire() {
        return refreshTokenExpire;
    }

    /** 计算指定 Token 剩余有效秒数（用于黑名单 TTL） */
    public long getRemainingExpiration(Claims claims) {
        long now = System.currentTimeMillis();
        long exp = claims.getExpiration().getTime();
        return Math.max(0, (exp - now) / 1000);
    }
}

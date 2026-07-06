package com.aegis.shujukuhouduan.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类 —— 负责token的生成、解析、验证
 * 使用HMAC-SHA256签名算法
 * 密钥和过期时间在application.yaml中配置
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;          // JWT签名密钥（至少32字节）

    @Value("${jwt.expiration}")
    private long expiration;        // token过期时间（毫秒），默认24小时

    /** 根据密钥字符串生成HMAC签名密钥 */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT token
     * @param username 用户名（作为subject）
     * @param role     角色（user/admin）
     * @param userId   用户ID
     * @return 签名后的JWT字符串
     */
    public String generateToken(String username, String role, Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)              // 设置主题（用户名）
                .claim("role", role)            // 自定义声明：角色
                .claim("userId", userId)        // 自定义声明：用户ID
                .issuedAt(now)                  // 签发时间
                .expiration(expiryDate)         // 过期时间
                .signWith(getSigningKey())      // 签名
                .compact();
    }

    /** 从token中解析用户名 */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /** 从token中解析用户ID */
    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Integer.class);
    }

    /** 从token中解析角色 */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /** 验证token是否有效（未过期且签名正确） */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 解析token，返回Claims（包含所有声明信息） */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.aegis.shujukuhouduan.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器 —— 每次请求都会执行
 * 从请求头Authorization中提取Bearer token，验证后设置SecurityContext
 * 这样后续的Controller就能通过SecurityContextHolder获取当前用户信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头提取token
        String token = extractToken(request);

        // 2. token有效则设置认证信息
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            Integer userId = jwtUtil.getUserIdFromToken(token);

            // 构建权限对象（Spring Security要求ROLE_前缀）
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

            // 创建认证token，principal=username，credentials=null，authorities=[ROLE_XXX]
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));

            // ⭐ 将userId存入details，后续Controller可通过auth.getDetails()获取
            authentication.setDetails(userId);

            // 设置到SecurityContext中，整个请求链都可以使用
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }

    /** 从Authorization请求头提取token（格式：Bearer xxxxxx） */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 去掉"Bearer "前缀
        }
        return null;
    }
}

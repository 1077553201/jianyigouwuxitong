package com.aegis.shujukuhouduan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置 —— 设置跨域资源共享（CORS）
 * 前端页面和后端API可能不在同一个端口/域名，需要允许跨域请求
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                          // 所有接口路径
                .allowedOriginPatterns("*")                  // 允许所有来源（开发阶段）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
                .allowedHeaders("*")                         // 允许所有请求头
                .allowCredentials(true)                      // 允许携带cookie/token
                .maxAge(3600);                               // 预检请求缓存1小时
    }
}

package com.aegis.shujukuhouduan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应体 —— 包含JWT token和用户基本信息
 * 前端收到后需将token保存到localStorage，后续请求放到Authorization头
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT token（前端保存后，每次请求放到 Header: Authorization: Bearer {token}） */
    private String token;

    /** 用户ID */
    private Integer userId;

    /** 用户名 */
    private String username;

    /** 角色：user/admin */
    private String role;
}

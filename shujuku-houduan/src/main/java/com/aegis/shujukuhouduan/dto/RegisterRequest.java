package com.aegis.shujukuhouduan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求体
 */
@Data
public class RegisterRequest {

    /** 用户名（3-50个字符） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50个字符")
    private String username;

    /** 密码（6-50个字符） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50个字符")
    private String password;

    /** 邮箱（可选） */
    private String email;

    /** 手机号（可选） */
    private String phone;
}

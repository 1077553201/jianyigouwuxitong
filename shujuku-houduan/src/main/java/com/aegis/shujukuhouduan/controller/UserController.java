package com.aegis.shujukuhouduan.controller;

import com.aegis.shujukuhouduan.dto.ApiResponse;
import com.aegis.shujukuhouduan.entity.User;
import com.aegis.shujukuhouduan.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器 —— 个人信息查看和修改
 * 需要登录（携带JWT token）
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前登录用户的个人信息
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile() {
        Integer userId = getCurrentUserId();
        User user = userService.getUserById(userId);

        // 组装返回数据（不包含密码）
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("role", user.getRole());
        profile.put("createdAt", user.getCreatedAt());

        return ApiResponse.success(profile);
    }

    /**
     * 修改个人信息（邮箱、手机号）
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestParam(required = false) String email,
                                           @RequestParam(required = false) String phone) {
        Integer userId = getCurrentUserId();
        userService.updateUser(userId, email, phone);
        return ApiResponse.success("修改成功", null);
    }

    /**
     * 从SecurityContext中获取当前登录用户ID
     * userId在JwtAuthenticationFilter中存入authentication.details
     */
    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Integer) auth.getDetails();
    }
}

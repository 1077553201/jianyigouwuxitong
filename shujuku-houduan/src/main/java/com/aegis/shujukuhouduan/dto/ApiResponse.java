package com.aegis.shujukuhouduan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式
 * 所有接口都返回此格式，前端统一按 code/message/data 解析
 *
 * 成功示例：{"code": 200, "message": "success", "data": {...}}
 * 失败示例：{"code": 400, "message": "用户名已存在", "data": null}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** 状态码：200=成功，400=参数错误，401=未登录，403=权限不足，500=服务器错误 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据（成功时返回，失败时为null） */
    private T data;

    /** 成功（有数据） */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /** 成功（有提示+数据） */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /** 成功（无数据） */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    /** 失败（自定义状态码） */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /** 失败（默认400） */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }
}

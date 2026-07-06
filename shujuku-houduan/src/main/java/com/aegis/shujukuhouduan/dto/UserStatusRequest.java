package com.aegis.shujukuhouduan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员更新用户状态请求体
 */
@Data
public class UserStatusRequest {

    /** 用户状态：1=正常，0=禁用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}

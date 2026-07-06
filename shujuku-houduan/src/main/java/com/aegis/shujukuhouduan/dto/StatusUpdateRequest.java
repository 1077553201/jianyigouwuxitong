package com.aegis.shujukuhouduan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用状态更新请求体（用于禁用用户、更新订单状态等）
 */
@Data
public class StatusUpdateRequest {

    /** 新状态值 */
    @NotNull(message = "状态不能为空")
    private String status;
}

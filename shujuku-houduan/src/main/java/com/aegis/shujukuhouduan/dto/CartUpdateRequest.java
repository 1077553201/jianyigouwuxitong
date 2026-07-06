package com.aegis.shujukuhouduan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改购物车数量请求体
 */
@Data
public class CartUpdateRequest {

    /** 新数量（设为0则删除该商品） */
    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能为负数")
    private Integer quantity;
}

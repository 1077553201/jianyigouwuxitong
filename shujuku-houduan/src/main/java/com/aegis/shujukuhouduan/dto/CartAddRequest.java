package com.aegis.shujukuhouduan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加购物车请求体
 */
@Data
public class CartAddRequest {

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    /** 数量，默认1 */
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;
}

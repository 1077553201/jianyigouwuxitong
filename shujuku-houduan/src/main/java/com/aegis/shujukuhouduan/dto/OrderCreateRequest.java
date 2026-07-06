package com.aegis.shujukuhouduan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求体
 */
@Data
public class OrderCreateRequest {

    /** 收货地址 */
    @NotBlank(message = "收货地址不能为空")
    private String address;

    /** 收货电话 */
    @NotBlank(message = "收货电话不能为空")
    private String phone;

    /** 订单商品列表 */
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemRequest> items;

    /**
     * 订单中的单个商品项
     */
    @Data
    public static class OrderItemRequest {
        /** 商品ID */
        private Integer productId;
        /** 购买数量 */
        private Integer quantity;
    }
}

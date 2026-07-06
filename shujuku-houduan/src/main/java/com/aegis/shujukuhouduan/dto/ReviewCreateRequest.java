package com.aegis.shujukuhouduan.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发表评价请求体
 */
@Data
public class ReviewCreateRequest {

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    /** 订单ID（可选，标记是哪个订单的评价） */
    private Integer orderId;

    /** 评分：1-5 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer rating;

    /** 评价内容 */
    private String content;
}

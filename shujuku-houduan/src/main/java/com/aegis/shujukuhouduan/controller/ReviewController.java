package com.aegis.shujukuhouduan.controller;

import com.aegis.shujukuhouduan.dto.ApiResponse;
import com.aegis.shujukuhouduan.dto.PageResponse;
import com.aegis.shujukuhouduan.dto.ReviewCreateRequest;
import com.aegis.shujukuhouduan.entity.Review;
import com.aegis.shujukuhouduan.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器 —— 发表评价和查看商品评价
 */
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 发表评价（需要登录）
     * POST /api/reviews
     * body: {"productId": 1, "orderId": 1, "rating": 5, "content": "很好"}
     */
    @PostMapping("/reviews")
    public ApiResponse<Review> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        Integer userId = getCurrentUserId();
        Review review = reviewService.createReview(
                userId, request.getProductId(), request.getOrderId(),
                request.getRating(), request.getContent());
        return ApiResponse.success("评价成功", review);
    }

    /**
     * 查看商品的评价列表（公开接口）
     * GET /api/products/1/reviews?page=0&size=10
     */
    @GetMapping("/products/{productId}/reviews")
    public ApiResponse<PageResponse<Review>> getProductReviews(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(reviewService.getProductReviews(productId, page, size));
    }

    /** 从SecurityContext中获取当前登录用户ID */
    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Integer) auth.getDetails();
    }
}

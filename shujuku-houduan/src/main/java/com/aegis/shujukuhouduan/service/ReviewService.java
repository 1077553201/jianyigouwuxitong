package com.aegis.shujukuhouduan.service;

import com.aegis.shujukuhouduan.dto.PageResponse;
import com.aegis.shujukuhouduan.entity.Review;
import com.aegis.shujukuhouduan.exception.BusinessException;
import com.aegis.shujukuhouduan.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 评价业务层 —— 商品评价的发表和查询
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * 发表评价
     * @param userId    评价用户ID
     * @param productId 商品ID
     * @param orderId   关联订单ID（可选）
     * @param rating    评分1-5
     * @param content   评价内容
     */
    public Review createReview(Integer userId, Integer productId, Integer orderId, Integer rating, String content) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content);
        review.setStatus(1); // 默认显示

        return reviewRepository.save(review);
    }

    /** 获取商品的评价列表（分页，只返回显示状态的评价） */
    public PageResponse<Review> getProductReviews(Integer productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, 1, pageable);

        return new PageResponse<>(
                reviewPage.getContent(),
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages()
        );
    }
}

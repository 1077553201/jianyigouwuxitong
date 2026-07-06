package com.aegis.shujukuhouduan.repository;

import com.aegis.shujukuhouduan.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 商品评价数据访问层
 */
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /** 查询商品的评价列表（只返回显示状态，按时间倒序） */
    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(Integer productId, Integer status, Pageable pageable);

    /** 查询用户的评价列表 */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);
}

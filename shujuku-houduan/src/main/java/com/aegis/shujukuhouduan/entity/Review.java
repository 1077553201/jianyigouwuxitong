package com.aegis.shujukuhouduan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 商品评价实体类 —— 对应数据库 reviews 表
 * 用户对已购商品的评分和文字评价
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    /** 评价ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 评价用户ID */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** 被评价的商品ID */
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    /** 关联的订单ID（可选） */
    @Column(name = "order_id")
    private Integer orderId;

    /** 评分：1-5星 */
    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer rating;

    /** 评价内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 状态：1=显示，0=隐藏 */
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /** 评价时间 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 评价用户（懒加载） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User user;
}

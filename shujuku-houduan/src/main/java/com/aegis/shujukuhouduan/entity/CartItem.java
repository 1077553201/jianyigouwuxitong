package com.aegis.shujukuhouduan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 购物车实体类 —— 对应数据库 cart_items 表
 * 复合唯一键：(user_id, product_id)，同一用户同一商品不会重复
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_product", columnNames = {"user_id", "product_id"})
})
public class CartItem {

    /** 购物车记录ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 用户ID */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    /** 购买数量，默认1 */
    @Column(nullable = false)
    private Integer quantity = 1;

    /** 添加时间 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 关联的商品信息（懒加载，方便前端直接展示） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;
}

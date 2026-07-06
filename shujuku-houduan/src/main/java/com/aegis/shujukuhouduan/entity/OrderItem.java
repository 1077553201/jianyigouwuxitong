package com.aegis.shujukuhouduan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类 —— 对应数据库 order_items 表
 * 记录每个订单中的具体商品信息
 * ⭐ price 字段是"价格快照"，存储下单时的价格，不受后续商品调价影响
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    /** 订单明细ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 所属订单ID */
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    /** 购买数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** ⭐ 下单时的商品价格（价格快照），与商品表价格独立 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 创建时间 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 所属订单（懒加载，JSON序列化时忽略循环引用） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"orderItems"})
    private Order order;

    /** 对应商品（懒加载） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;
}

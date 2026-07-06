package com.aegis.shujukuhouduan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类 —— 对应数据库 products 表
 * 包含名称、描述、价格、库存、分类、图片等信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    /** 商品ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 商品名称 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 商品描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 售价（精确到分） */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 库存数量 */
    @Column(nullable = false)
    private Integer stock = 0;

    /** 所属分类ID */
    @Column(name = "category_id")
    private Integer categoryId;

    /** 商品图片URL */
    @Column(name = "image_url", length = 300)
    private String imageUrl;

    /** 状态：1=上架，0=下架 */
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /** 创建时间 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 所属分类（懒加载，JSON序列化时忽略循环引用） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;
}

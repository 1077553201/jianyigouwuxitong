package com.aegis.shujukuhouduan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 商品分类实体类 —— 对应数据库 categories 表
 * 支持多级分类：通过 parentId 实现自关联
 * parentId=0 表示一级分类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    /** 分类ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 分类名称 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 父分类ID，0=一级分类 */
    @Column(name = "parent_id")
    private Integer parentId = 0;

    /** 排序序号，数值越小越靠前 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** 状态：1=启用，0=禁用 */
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /** 创建时间 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

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
import java.util.List;

/**
 * 订单实体类 —— 对应数据库 orders 表
 * 一个订单包含多个订单明细（order_items）
 * 状态流转：pending -> paid -> shipped -> completed
 *           pending -> cancelled（用户取消）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    /** 订单ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 订单号（时间戳+随机数，如 202606171430521234） */
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    /** 下单用户ID */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** 订单总金额 */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** 订单状态：pending=待付款, paid=已付款, shipped=已发货, completed=已完成, cancelled=已取消 */
    @Column(length = 20, columnDefinition = "ENUM('pending','paid','shipped','completed','cancelled')")
    private String status = "pending";

    /** 收货地址 */
    @Column(length = 500)
    private String address;

    /** 收货电话 */
    @Column(length = 20)
    private String phone;

    /** 创建时间（下单时间） */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 下单用户（懒加载，JSON序列化时忽略） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    /** 订单明细列表（懒加载，JSON序列化时忽略循环引用） */
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"order"})
    private List<OrderItem> orderItems;
}

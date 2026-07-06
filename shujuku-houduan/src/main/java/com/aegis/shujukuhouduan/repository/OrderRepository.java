package com.aegis.shujukuhouduan.repository;

import com.aegis.shujukuhouduan.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 订单数据访问层
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {

    /** 用户的订单列表（按创建时间倒序） */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    /** 所有订单列表（按创建时间倒序） */
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 按状态筛选订单 */
    Page<Order> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}

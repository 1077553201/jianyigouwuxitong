package com.aegis.shujukuhouduan.repository;

import com.aegis.shujukuhouduan.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 订单明细数据访问层
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    /** 查询订单下的所有明细 */
    List<OrderItem> findByOrderId(Integer orderId);
}

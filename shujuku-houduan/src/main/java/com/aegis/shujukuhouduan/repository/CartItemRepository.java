package com.aegis.shujukuhouduan.repository;

import com.aegis.shujukuhouduan.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 购物车数据访问层
 */
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    /** 用户的购物车列表（按添加时间倒序） */
    List<CartItem> findByUserIdOrderByCreatedAtDesc(Integer userId);

    /** 查找用户购物车中某个商品（判断是否已存在） */
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);

    /** 清空用户的购物车 */
    void deleteByUserId(Integer userId);

    /** 购物车数量累加（利用复合唯一键避免并发问题） */
    @Modifying
    @Transactional
    @Query("UPDATE CartItem c SET c.quantity = c.quantity + :quantity WHERE c.userId = :userId AND c.productId = :productId")
    int incrementQuantity(@Param("userId") Integer userId, @Param("productId") Integer productId, @Param("quantity") Integer quantity);

    /** 批量删除购物车项（下单后清理已购买的商品） */
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId AND c.productId IN :productIds")
    void deleteByUserIdAndProductIdIn(@Param("userId") Integer userId, @Param("productIds") List<Integer> productIds);
}

package com.aegis.shujukuhouduan.service;

import com.aegis.shujukuhouduan.entity.CartItem;
import com.aegis.shujukuhouduan.exception.BusinessException;
import com.aegis.shujukuhouduan.repository.CartItemRepository;
import com.aegis.shujukuhouduan.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 购物车业务层 —— 购物车的增删改查
 * 核心逻辑：同一用户添加相同商品时，数量累加而非新增记录
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /** 获取用户的购物车列表（按添加时间倒序） */
    public List<CartItem> getCartItems(Integer userId) {
        return cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 添加商品到购物车
     * 如果该商品已在购物车中，则数量累加；否则新增一条记录
     */
    public CartItem addToCart(Integer userId, Integer productId, Integer quantity) {
        // 验证商品存在且上架
        productRepository.findById(productId)
                .filter(p -> p.getStatus() == 1)
                .orElseThrow(() -> new BusinessException("商品不存在或已下架"));

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);

        if (existing.isPresent()) {
            // 已存在 → 数量累加
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            // 不存在 → 新增
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            return cartItemRepository.save(item);
        }
    }

    /**
     * 修改购物车商品数量
     * 数量为0则删除该商品
     */
    public CartItem updateQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("购物车项不存在"));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此购物车项");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    /** 删除购物车中的某件商品 */
    public void removeCartItem(Integer userId, Integer cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("购物车项不存在"));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此购物车项");
        }

        cartItemRepository.delete(item);
    }

    /** 清空用户的整个购物车 */
    public void clearCart(Integer userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}

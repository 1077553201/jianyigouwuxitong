package com.aegis.shujukuhouduan.controller;

import com.aegis.shujukuhouduan.dto.ApiResponse;
import com.aegis.shujukuhouduan.dto.CartAddRequest;
import com.aegis.shujukuhouduan.dto.CartUpdateRequest;
import com.aegis.shujukuhouduan.entity.CartItem;
import com.aegis.shujukuhouduan.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器 —— 购物车的增删改查
 * 需要登录（携带JWT token）
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 获取当前用户的购物车列表（含商品信息）
     * GET /api/cart
     */
    @GetMapping
    public ApiResponse<List<CartItem>> getCartItems() {
        Integer userId = getCurrentUserId();
        return ApiResponse.success(cartService.getCartItems(userId));
    }

    /**
     * 添加商品到购物车（同商品自动累加数量）
     * POST /api/cart  body: {"productId": 1, "quantity": 2}
     */
    @PostMapping
    public ApiResponse<CartItem> addToCart(@Valid @RequestBody CartAddRequest request) {
        Integer userId = getCurrentUserId();
        CartItem item = cartService.addToCart(userId, request.getProductId(), request.getQuantity());
        return ApiResponse.success("添加成功", item);
    }

    /**
     * 修改购物车商品数量
     * PUT /api/cart/{id}  body: {"quantity": 5}
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateQuantity(@PathVariable Integer id,
                                            @Valid @RequestBody CartUpdateRequest request) {
        Integer userId = getCurrentUserId();
        cartService.updateQuantity(userId, id, request.getQuantity());
        return ApiResponse.success("修改成功", null);
    }

    /**
     * 删除购物车中的某件商品
     * DELETE /api/cart/1
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeCartItem(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        cartService.removeCartItem(userId, id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 清空购物车
     * DELETE /api/cart
     */
    @DeleteMapping
    public ApiResponse<Void> clearCart() {
        Integer userId = getCurrentUserId();
        cartService.clearCart(userId);
        return ApiResponse.success("清空成功", null);
    }

    /** 从SecurityContext中获取当前登录用户ID */
    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Integer) auth.getDetails();
    }
}

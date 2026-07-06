package com.aegis.shujukuhouduan.controller;

import com.aegis.shujukuhouduan.dto.*;
import com.aegis.shujukuhouduan.entity.Order;
import com.aegis.shujukuhouduan.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器 —— 用户下单、查看订单、取消订单
 * 需要登录（携带JWT token）
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单（⭐核心接口，含事务处理）
     * POST /api/orders
     * body: {
     *   "address": "北京市xxx",
     *   "phone": "13800000000",
     *   "items": [{"productId": 1, "quantity": 2}, {"productId": 3, "quantity": 1}]
     * }
     */
    @PostMapping
    public ApiResponse<Order> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Integer userId = getCurrentUserId();
        Order order = orderService.createOrder(userId, request);
        return ApiResponse.success("下单成功", order);
    }

    /**
     * 我的订单列表（分页）
     * GET /api/orders?page=0&size=10
     */
    @GetMapping
    public ApiResponse<PageResponse<Order>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Integer userId = getCurrentUserId();
        return ApiResponse.success(orderService.getUserOrders(userId, page, size));
    }

    /**
     * 订单详情（含订单明细）
     * GET /api/orders/1
     */
    @GetMapping("/{id}")
    public ApiResponse<Order> getOrderDetail(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        return ApiResponse.success(orderService.getOrderDetail(userId, id));
    }

    /**
     * 取消订单（仅pending状态可取消，会恢复库存）
     * PUT /api/orders/1/cancel
     */
    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        orderService.cancelOrder(userId, id);
        return ApiResponse.success("订单已取消", null);
    }

    /** 从SecurityContext中获取当前登录用户ID */
    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Integer) auth.getDetails();
    }
}

package com.aegis.shujukuhouduan.controller;

import com.aegis.shujukuhouduan.dto.*;
import com.aegis.shujukuhouduan.entity.Order;
import com.aegis.shujukuhouduan.entity.Product;
import com.aegis.shujukuhouduan.entity.User;
import com.aegis.shujukuhouduan.repository.UserRepository;
import com.aegis.shujukuhouduan.service.OrderService;
import com.aegis.shujukuhouduan.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台控制器 —— 用户管理、商品管理、订单管理
 * ⚠️ 所有接口需要管理员角色（ROLE_ADMIN）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ProductService productService;
    private final OrderService orderService;

    public AdminController(UserRepository userRepository,
                           ProductService productService,
                           OrderService orderService) {
        this.userRepository = userRepository;
        this.productService = productService;
        this.orderService = orderService;
    }

    // ==================== 用户管理 ====================

    /**
     * 用户列表（分页）
     * GET /api/admin/users?page=0&size=10
     */
    @GetMapping("/users")
    public ApiResponse<PageResponse<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        PageResponse<User> response = new PageResponse<>(
                userPage.getContent(), userPage.getNumber(), userPage.getSize(),
                userPage.getTotalElements(), userPage.getTotalPages());
        return ApiResponse.success(response);
    }

    /**
     * 禁用/启用用户
     * PUT /api/admin/users/1/status  body: {"status": 0}
     */
    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Integer id,
                                               @RequestBody UserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(request.getStatus());
        userRepository.save(user);
        return ApiResponse.success("状态更新成功", null);
    }

    // ==================== 商品管理 ====================

    /**
     * 新增商品
     * POST /api/admin/products
     * body: {"name": "新商品", "price": 99.00, "stock": 100, "categoryId": 1}
     */
    @PostMapping("/products")
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        return ApiResponse.success("商品创建成功", productService.createProduct(product));
    }

    /**
     * 修改商品信息
     * PUT /api/admin/products/1
     * body: {"name": "新名称", "price": 199.00}
     */
    @PutMapping("/products/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        return ApiResponse.success("商品更新成功", productService.updateProduct(id, product));
    }

    /**
     * 下架商品（软删除，status设为0）
     * DELETE /api/admin/products/1
     */
    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ApiResponse.success("商品已下架", null);
    }

    // ==================== 订单管理 ====================

    /**
     * 所有订单列表（支持按状态筛选）
     * GET /api/admin/orders?status=pending&page=0&size=10
     */
    @GetMapping("/orders")
    public ApiResponse<PageResponse<Order>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.getAllOrders(status, page, size));
    }

    /**
     * 更新订单状态（如发货：pending->shipped）
     * PUT /api/admin/orders/1/status  body: {"status": "shipped"}
     */
    @PutMapping("/orders/{id}/status")
    public ApiResponse<Order> updateOrderStatus(@PathVariable Integer id,
                                                 @RequestBody StatusUpdateRequest request) {
        return ApiResponse.success("订单状态更新成功",
                orderService.updateOrderStatus(id, request.getStatus()));
    }
}

package com.aegis.shujukuhouduan.service;

import com.aegis.shujukuhouduan.dto.OrderCreateRequest;
import com.aegis.shujukuhouduan.dto.PageResponse;
import com.aegis.shujukuhouduan.entity.Order;
import com.aegis.shujukuhouduan.entity.OrderItem;
import com.aegis.shujukuhouduan.entity.Product;
import com.aegis.shujukuhouduan.exception.BusinessException;
import com.aegis.shujukuhouduan.repository.OrderItemRepository;
import com.aegis.shujukuhouduan.repository.OrderRepository;
import com.aegis.shujukuhouduan.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单业务层 —— ⭐核心模块
 * 创建订单使用事务（@Transactional），保证数据一致性：
 * 1. 校验库存
 * 2. 创建订单头+明细
 * 3. 扣减库存
 * 4. 任一步骤失败则全部回滚
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * ⭐ 创建订单（事务处理核心接口）
     *
     * 事务保证：库存校验、订单创建、库存扣减在同一事务中
     * 任一步骤异常都会回滚，不会出现"订单创建了但库存没扣"的情况
     *
     * 价格快照：order_items.price 存储下单时的商品价格，不受后续调价影响
     */
    @Transactional
    public Order createOrder(Integer userId, OrderCreateRequest request) {
        // 1. 校验商品和库存，计算总金额
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderCreateRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new BusinessException("商品不存在: " + itemReq.getProductId()));

            if (product.getStatus() != 1) {
                throw new BusinessException("商品已下架: " + product.getName());
            }

            if (product.getStock() < itemReq.getQuantity()) {
                throw new BusinessException("库存不足: " + product.getName()
                        + "，当前库存: " + product.getStock());
            }

            // 创建订单明细（⭐价格快照：存当前价格，不是以后的价格）
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(product.getPrice()); // 快照
            orderItems.add(item);

            // 累加总金额 = 单价 × 数量
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        // 2. 生成唯一订单号（时间戳+4位随机数）
        String orderNo = generateOrderNo();

        // 3. 创建订单头
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");    // 初始状态：待付款
        order.setAddress(request.getAddress());
        order.setPhone(request.getPhone());
        order = orderRepository.save(order);

        // 4. 创建订单明细（关联订单ID）
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemRepository.saveAll(orderItems);

        // 5. 扣减库存（再次检查库存，防止并发超卖）
        for (OrderCreateRequest.OrderItemRequest itemReq : request.getItems()) {
            int updated = productRepository.findById(itemReq.getProductId())
                    .map(p -> {
                        if (p.getStock() >= itemReq.getQuantity()) {
                            p.setStock(p.getStock() - itemReq.getQuantity());
                            productRepository.save(p);
                            return 1;
                        }
                        return 0;
                    })
                    .orElse(0);

            if (updated == 0) {
                // 库存不足，抛出异常触发事务回滚
                throw new BusinessException("库存扣减失败，商品可能已被其他用户购买");
            }
        }

        return order;
    }

    /** 用户的订单列表（按创建时间倒序） */
    public PageResponse<Order> getUserOrders(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return new PageResponse<>(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    /** 订单详情（含明细列表，非管理员只能看自己的订单） */
    public Order getOrderDetail(Integer userId, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此订单");
        }

        // 手动加载订单明细（懒加载）
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        order.setOrderItems(items);

        return order;
    }

    /**
     * 取消订单（仅pending状态可取消）
     * 取消后恢复对应商品的库存
     */
    @Transactional
    public void cancelOrder(Integer userId, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }

        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException("只能取消待付款的订单");
        }

        // 恢复库存
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            productRepository.findById(item.getProductId()).ifPresent(p -> {
                p.setStock(p.getStock() + item.getQuantity());
                productRepository.save(p);
            });
        }

        order.setStatus("cancelled");
        orderRepository.save(order);
    }

    /** 管理员 - 所有订单列表（支持按状态筛选） */
    public PageResponse<Order> getAllOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage;

        if (status != null && !status.isBlank()) {
            orderPage = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            orderPage = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return new PageResponse<>(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    /** 管理员 - 更新订单状态（如发货、完成等） */
    public Order updateOrderStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * 生成订单号：时间戳(14位) + 随机数(4位)
     * 示例：202606171430521234
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return timestamp + random;
    }
}

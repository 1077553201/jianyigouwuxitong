package com.aegis.shujukuhouduan.controller;

import com.aegis.shujukuhouduan.dto.ApiResponse;
import com.aegis.shujukuhouduan.dto.PageResponse;
import com.aegis.shujukuhouduan.entity.Product;
import com.aegis.shujukuhouduan.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 商品控制器 —— 商品列表和详情（公开接口，无需登录）
 * 支持关键字搜索、分类筛选、价格区间、排序、分页
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品列表（分页+搜索+分类筛选+排序）
     * GET /api/products?keyword=手机&categoryId=5&sortBy=price&sortDir=asc&page=0&size=10
     */
    @GetMapping
    public ApiResponse<PageResponse<Product>> getProducts(
            @RequestParam(required = false) String keyword,       // 搜索关键字（模糊匹配商品名）
            @RequestParam(required = false) Integer categoryId,   // 分类ID筛选
            @RequestParam(required = false) BigDecimal minPrice,  // 最低价格
            @RequestParam(required = false) BigDecimal maxPrice,  // 最高价格
            @RequestParam(required = false) String sortBy,        // 排序字段：price/createdAt/stock
            @RequestParam(required = false, defaultValue = "desc") String sortDir, // 排序方向：asc/desc
            @RequestParam(defaultValue = "0") int page,           // 页码（从0开始）
            @RequestParam(defaultValue = "10") int size) {        // 每页条数

        PageResponse<Product> products = productService.getProducts(
                keyword, categoryId, minPrice, maxPrice, sortBy, sortDir, page, size);
        return ApiResponse.success(products);
    }

    /**
     * 商品详情
     * GET /api/products/1
     */
    @GetMapping("/{id}")
    public ApiResponse<Product> getProduct(@PathVariable Integer id) {
        Product product = productService.getProductById(id);
        return ApiResponse.success(product);
    }
}

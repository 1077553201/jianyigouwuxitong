package com.aegis.shujukuhouduan.service;

import com.aegis.shujukuhouduan.dto.PageResponse;
import com.aegis.shujukuhouduan.entity.Product;
import com.aegis.shujukuhouduan.exception.BusinessException;
import com.aegis.shujukuhouduan.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 商品业务层 —— 商品查询、搜索、管理
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 商品列表（分页+搜索+分类筛选+价格排序）
     * 只返回上架商品（status=1）
     *
     * @param keyword    搜索关键字（模糊匹配商品名）
     * @param categoryId 分类ID
     * @param minPrice   最低价格
     * @param maxPrice   最高价格
     * @param sortBy     排序字段（price/createdAt/stock）
     * @param sortDir    排序方向（asc/desc）
     * @param page       页码（从0开始）
     * @param size       每页条数
     */
    public PageResponse<Product> getProducts(String keyword, Integer categoryId,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             String sortBy, String sortDir,
                                             int page, int size) {
        String field = sortBy != null ? sortBy : "createdAt";
        boolean isDesc = !"asc".equalsIgnoreCase(sortDir);
        Sort sort = isDesc ? Sort.by(field).descending() : Sort.by(field).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage;

        // 根据参数组合选择查询方式
        if (keyword != null && !keyword.isBlank() && categoryId != null) {
            // 关键字 + 分类
            productPage = productRepository.searchByNameAndCategory(1, keyword, categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            // 仅关键字搜索
            productPage = productRepository.searchByName(1, keyword, pageable);
        } else if (categoryId != null) {
            // 仅分类筛选
            productPage = productRepository.findByStatusAndCategoryId(1, categoryId, pageable);
        } else if (minPrice != null && maxPrice != null) {
            // 价格区间
            productPage = productRepository.findByPriceRange(1, minPrice, maxPrice, pageable);
        } else {
            // 全部上架商品
            productPage = productRepository.findByStatus(1, pageable);
        }

        return new PageResponse<>(
                productPage.getContent(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }

    /** 商品详情（必须是上架状态） */
    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (product.getStatus() == 0) {
            throw new BusinessException("商品已下架");
        }
        return product;
    }

    /** 管理员 - 新增商品（默认上架状态） */
    public Product createProduct(Product product) {
        product.setStatus(1);
        return productRepository.save(product);
    }

    /** 管理员 - 修改商品（只更新非空字段） */
    public Product updateProduct(Integer id, Product updateData) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (updateData.getName() != null) product.setName(updateData.getName());
        if (updateData.getDescription() != null) product.setDescription(updateData.getDescription());
        if (updateData.getPrice() != null) product.setPrice(updateData.getPrice());
        if (updateData.getStock() != null) product.setStock(updateData.getStock());
        if (updateData.getCategoryId() != null) product.setCategoryId(updateData.getCategoryId());
        if (updateData.getImageUrl() != null) product.setImageUrl(updateData.getImageUrl());

        return productRepository.save(product);
    }

    /** 管理员 - 下架商品（软删除，status设为0） */
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setStatus(0);
        productRepository.save(product);
    }
}

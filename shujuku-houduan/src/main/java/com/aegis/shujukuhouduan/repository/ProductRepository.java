package com.aegis.shujukuhouduan.repository;

import com.aegis.shujukuhouduan.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/**
 * 商品数据访问层
 * 包含分页查询、关键字搜索、分类筛选、价格区间等自定义查询
 */
public interface ProductRepository extends JpaRepository<Product, Integer> {

    /** 按状态分页查询（默认查询上架商品） */
    Page<Product> findByStatus(Integer status, Pageable pageable);

    /** 按状态+分类查询 */
    Page<Product> findByStatusAndCategoryId(Integer status, Integer categoryId, Pageable pageable);

    /** 按名称模糊搜索（只搜上架商品） */
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.name LIKE %:keyword%")
    Page<Product> searchByName(@Param("status") Integer status, @Param("keyword") String keyword, Pageable pageable);

    /** 按名称模糊搜索 + 分类筛选 */
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.name LIKE %:keyword% AND p.categoryId = :categoryId")
    Page<Product> searchByNameAndCategory(@Param("status") Integer status,
                                          @Param("keyword") String keyword,
                                          @Param("categoryId") Integer categoryId,
                                          Pageable pageable);

    /** 按价格区间查询 */
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("status") Integer status,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);
}

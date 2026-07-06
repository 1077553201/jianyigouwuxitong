package com.aegis.shujukuhouduan.repository;

import com.aegis.shujukuhouduan.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 商品分类数据访问层
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /** 查找指定父分类下的子分类（按排序号升序） */
    List<Category> findByParentIdAndStatusOrderBySortOrderAsc(Integer parentId, Integer status);

    /** 查找所有启用的分类（按排序号升序） */
    List<Category> findByStatusOrderBySortOrderAsc(Integer status);
}

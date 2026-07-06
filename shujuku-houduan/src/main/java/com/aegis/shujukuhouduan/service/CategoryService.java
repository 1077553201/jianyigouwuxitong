package com.aegis.shujukuhouduan.service;

import com.aegis.shujukuhouduan.entity.Category;
import com.aegis.shujukuhouduan.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务层 —— 商品分类查询
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /** 获取所有启用的分类（按排序号升序） */
    public List<Category> getAllCategories() {
        return categoryRepository.findByStatusOrderBySortOrderAsc(1);
    }

    /** 获取指定父分类下的子分类 */
    public List<Category> getSubCategories(Integer parentId) {
        return categoryRepository.findByParentIdAndStatusOrderBySortOrderAsc(parentId, 1);
    }
}

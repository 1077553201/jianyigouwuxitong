package com.aegis.shujukuhouduan.controller;

import com.aegis.shujukuhouduan.dto.ApiResponse;
import com.aegis.shujukuhouduan.entity.Category;
import com.aegis.shujukuhouduan.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<Category>> getAllCategories() {
        return ApiResponse.success(categoryService.getAllCategories());
    }

    @GetMapping("/{parentId}/sub")
    public ApiResponse<List<Category>> getSubCategories(@PathVariable Integer parentId) {
        return ApiResponse.success(categoryService.getSubCategories(parentId));
    }
}

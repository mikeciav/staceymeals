package com.ciav.staceymeals.controller;

import com.ciav.staceymeals.model.Category;
import com.ciav.staceymeals.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@EnableWebMvc
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("users/{userId}/categories")
    public ResponseEntity<Category> createCategory(
            @PathVariable("userId") UUID userId,
            @RequestBody Category category) {
        category.setUserId(userId);
        Category createdCategory = categoryService.upsertCategory(category);
        return ResponseEntity.ok(createdCategory);
    }

    @GetMapping("users/{userId}/categories/{categoryId}")
    public ResponseEntity<Category> getCategory(
            @PathVariable("userId") UUID userId,
            @PathVariable("categoryId") UUID categoryId){

        return ResponseEntity.ok(categoryService.getCategory(userId, categoryId));
    }

    @GetMapping("users/{userId}/categories/")
    public ResponseEntity<List<Category>> getFullCategoryTree(
            @PathVariable("userId") UUID userId){

        return ResponseEntity.ok(categoryService.getFullCategoryTree(userId));
    }
}

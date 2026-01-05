package com.ciav.staceymeals.service;

import com.ciav.staceymeals.db.dao.CategoryDao;
import com.ciav.staceymeals.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Slf4j
public class CategoryService {

    private final CategoryDao categoryDao;

    @Autowired
    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public Category upsertCategory(Category category) {
        // Generate ID if not present (insert)
        if (category.getId() == null) {
            category.setId(UUID.randomUUID());
        }

        UUID savedId = categoryDao.save(category);
        category.setId(savedId);

        log.info("Saved category: {}", category);
        return category;
    }

    public Category getCategory(UUID userId, UUID categoryId) {
        return categoryDao.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> {
                    String msg = "Category not found. Category ID: " + categoryId + ", User ID: " + userId;
                    log.error(msg);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
                });
    }

    public List<Category> getFullCategoryTree(UUID userId) {
        List<Category> categories = categoryDao.findByUserId(userId);
        List<Category> rootCategories = new ArrayList<>();

        Map<UUID, Category> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category);
        }

        for (Category category : categories) {
            if (category.getParentCategoryId() != null) {
                Category parent = categoryMap.get(category.getParentCategoryId());
                if (parent != null) {
                    parent.getSubCategories().add(category);
                }
            } else {
                rootCategories.add(category);
            }
        }
        return rootCategories;
    }
}
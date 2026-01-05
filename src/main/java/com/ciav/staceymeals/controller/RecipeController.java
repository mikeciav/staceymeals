package com.ciav.staceymeals.controller;

import com.ciav.staceymeals.model.Recipe;
import com.ciav.staceymeals.model.RecipesCategories;
import com.ciav.staceymeals.service.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/recipes")
@EnableWebMvc
@Slf4j
public class RecipeController {
    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("/fetch-and-save")
    public ResponseEntity<Recipe> fetchAndSaveRecipe(
            @PathVariable("userId") UUID userId,
            @RequestBody String url) {
        Recipe recipe = recipeService.fetchAndSaveRecipe(userId, url);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping
    public ResponseEntity<Map<UUID, Recipe>> getRecipes(@PathVariable("userId") UUID userId) {
        Map<UUID, Recipe> recipe = recipeService.getRecipes(userId);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping("/{recipeId}")
    public ResponseEntity<Recipe> getRecipe(
            @PathVariable("userId") UUID userId,
            @PathVariable("recipeId") UUID recipeId) {
        Recipe recipe = recipeService.getRecipe(userId, recipeId);
        return ResponseEntity.ok(recipe);
    }

    @PutMapping("/{recipeId}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable("userId") UUID userId,
            @PathVariable("recipeId") UUID recipeId,
            @RequestBody Recipe updatedRecipe) {
        Recipe recipe = recipeService.updateRecipe(userId, recipeId, updatedRecipe);
        return ResponseEntity.ok(recipe);
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Recipe> deleteRecipe(
            @PathVariable("userId") UUID userId,
            @PathVariable("recipeId") UUID recipeId) {
        recipeService.deleteRecipe(userId, recipeId);
        return ResponseEntity.noContent().build();
    }

    // Category endpoints

    @PostMapping("/{recipeId}/categories/{categoryId}/categorize")
    public ResponseEntity<RecipesCategories> categorizeRecipe(
            @PathVariable("userId") UUID userId,
            @PathVariable("recipeId") UUID recipeId,
            @PathVariable("categoryId") UUID categoryId) {
        RecipesCategories createdCategory = recipeService.categorizeRecipe(recipeId, categoryId);
        return ResponseEntity.ok(createdCategory);
    }

    @DeleteMapping("/{recipeId}/categories/{categoryId}/uncategorize")
    public ResponseEntity<RecipesCategories> uncategorizeRecipe(
            @PathVariable("userId") UUID userId,
            @PathVariable("recipeId") UUID recipeId,
            @PathVariable("categoryId") UUID categoryId) {
        recipeService.uncategorizeRecipe(recipeId, categoryId);
        return ResponseEntity.noContent().build();
    }
}

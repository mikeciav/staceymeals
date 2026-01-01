package com.ciav.staceymeals.controller;

import com.ciav.staceymeals.model.FetchRecipeRequest;
import com.ciav.staceymeals.model.Recipe;
import com.ciav.staceymeals.service.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

@RestController
@RequestMapping("/api")
@EnableWebMvc
@Slf4j
public class RecipeController {
    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("users/{userId}/recipes/fetch-and-save")
    public ResponseEntity<Recipe> fetchAndSaveRecipe(
            @PathVariable("userId") String userId,
            @RequestBody FetchRecipeRequest request) {
        request.setUserId(userId);
        Recipe recipe = recipeService.fetchAndSaveRecipe(request);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping("users/{userId}/recipes")
    public ResponseEntity<List<Recipe>> getRecipes(@PathVariable("userId") String userId) {
        List<Recipe> recipe = recipeService.getRecipesForUser(userId);
        return ResponseEntity.ok(recipe);
    }

    @PutMapping("users/{userId}/recipes/{recipeId}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable("userId") String userId,
            @PathVariable("recipeId") String recipeId,
            @RequestBody Recipe updatedRecipe) {
        Recipe recipe = recipeService.updateRecipe(userId, recipeId, updatedRecipe);
        return ResponseEntity.ok(recipe);
    }

    @DeleteMapping("users/{userId}/recipes/{recipeId}")
    public ResponseEntity<Recipe> deleteRecipe(
            @PathVariable("userId") String userId,
            @PathVariable("recipeId") String recipeId) {
        recipeService.deleteRecipe(userId, recipeId);
        return ResponseEntity.noContent().build();
    }
}

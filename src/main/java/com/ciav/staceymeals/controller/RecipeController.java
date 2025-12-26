package com.ciav.staceymeals.controller;

import com.ciav.staceymeals.model.FetchRecipeRequest;
import com.ciav.staceymeals.model.Recipe;
import com.ciav.staceymeals.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import lombok.extern.slf4j.Slf4j;

@RestController
@EnableWebMvc
@Slf4j
public class RecipeController {
    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("/recipes/fetch-and-save")
    public ResponseEntity<Recipe> fetchAndSaveRecipe(@RequestBody FetchRecipeRequest request) {
        Recipe recipe = recipeService.fetchAndSaveRecipe(request);
        return ResponseEntity.ok(recipe);
    }
}

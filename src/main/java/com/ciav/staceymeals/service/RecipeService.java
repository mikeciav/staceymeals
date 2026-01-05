package com.ciav.staceymeals.service;

import com.ciav.staceymeals.db.dao.RecipeCategoriesDao;
import com.ciav.staceymeals.db.dao.RecipeDao;
import com.ciav.staceymeals.model.Category;
import com.ciav.staceymeals.model.Recipe;
import com.ciav.staceymeals.model.RecipesCategories;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.threeten.extra.AmountFormats;
import org.threeten.extra.PeriodDuration;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class RecipeService {

    private final RecipeDao recipeDao;
    private final RecipeCategoriesDao recipeCategoriesDao;

    @Autowired
    public RecipeService(RecipeDao recipeDao, RecipeCategoriesDao recipeCategoriesDao) {
        this.recipeDao = recipeDao;
        this.recipeCategoriesDao = recipeCategoriesDao;
    }

    public Recipe fetchAndSaveRecipe(UUID userId, String url) {
        Recipe recipe = extractRecipe(url);
        recipe.setUserId(userId);
        return saveRecipe(recipe);
    }

    public Recipe saveRecipe(Recipe recipe) {
        UUID savedId = recipeDao.save(recipe);
        recipe.setId(savedId);

        if (recipe.getCategories() != null) {
            List <RecipesCategories> recipesCategories = new ArrayList<>();
            for (Category c : recipe.getCategories()) {
                recipesCategories.add(new RecipesCategories(recipe.getId(), c.getId()));
            }
            recipeCategoriesDao.saveBatch(recipesCategories);
        }

        log.info("Saved recipe: {}", recipe);
        return recipe;
    }

    public Recipe extractRecipe(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; StaceyMealsBot/1.0)")
                    .timeout(10_000)
                    .get();

            Recipe recipe = parseJsonLdRecipe(doc, url);
            log.info("Extracted recipe using JSON-LD from URL: {}", url);
            return recipe;

        } catch (IOException e) {
            log.error("Error fetching URL {}: {}", url, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private Recipe parseJsonLdRecipe(Document doc, String sourceUrl) {
        Elements scripts = doc.select("script[type=application/ld+json]");
        for (Element script : scripts) {
            String json = script.html();
            if (json.contains("\"@type\"") && json.toLowerCase().contains("recipe")) {
                List<String> ingredients = extractJsonArrayField(json, "recipeIngredient");
                if (ingredients.isEmpty())
                    ingredients = extractJsonArrayField(json, "ingredients");

                List<String> steps = extractJsonTextFromInstructions(json);
                String thumbnailUrl = extractJsonField(json, "thumbnailUrl").orElse("");

                String prepTime = extractTime(extractJsonField(json, "prepTime").orElse(""));
                String cookTime = extractTime(extractJsonField(json, "cookTime").orElse(""));
                String totalTime = extractTime(extractJsonField(json, "totalTime").orElse(""));
                String servings = extractJsonField(json, "recipeYield").orElse("0");

                return Recipe.builder()
                        .sourceUrl(sourceUrl)
                        .title(doc.title())
                        .ingredients(ingredients)
                        .steps(steps)
                        .thumbnailUrl(thumbnailUrl)
                        .raw(json)
                        .prepTime(prepTime)
                        .cookTime(cookTime)
                        .totalTime(totalTime)
                        .servings(servings)
                        .build();
            }
        }
        String msg = "No recipe data found at the provided URL: " + sourceUrl;
        log.error(msg);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
    }

    private Optional<String> extractJsonField(String json, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return Optional.of(m.group(1));
        return Optional.empty();
    }

    private List<String> extractJsonArrayField(String json, String field) {
        List<String> out = new ArrayList<>();
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (m.find()) {
            String inside = m.group(1);
            Pattern q = Pattern.compile("\\\"([^\\\"]+)\\\"");
            Matcher mq = q.matcher(inside);
            while (mq.find()) out.add(mq.group(1));
        }
        return out;
    }

    private List<String> extractJsonTextFromInstructions(String json) {
        List<String> out = new ArrayList<>();
        Pattern p = Pattern.compile("\\\"text\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
        Matcher m = p.matcher(json);
        while (m.find()) out.add(m.group(1));
        return out;
    }

    private String extractTime(String isoTime){
        String time = "";
        try {
            PeriodDuration pd = PeriodDuration.parse(isoTime);
            time = AmountFormats.wordBased(pd.getPeriod(), pd.getDuration(), Locale.getDefault());
        } catch (Exception e){
            log.error("Could not parse time via duration or period parsing: {}", isoTime);
        }
        return time;
    }

    public Map<UUID, Recipe> getRecipes(UUID userId) {
        Map<UUID, Recipe> recipes = new HashMap<>();
        List<Recipe> recipeList = recipeDao.findByUserId(userId);
        for(Recipe r : recipeList){
            recipes.put(r.getId(), r);
        }
        return recipes;
    }

    public Recipe getRecipe(UUID userId, UUID recipeId) {
        Recipe recipe = recipeDao.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> {
                    String msg = "Recipe not found. Recipe ID: " + recipeId + ", User ID: " + userId;
                    log.error(msg);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
                });

        return recipe;
    }

    public Recipe updateRecipe(UUID userId, UUID recipeId, Recipe updatedRecipe) {
        updatedRecipe.setId(recipeId);
        updatedRecipe.setUserId(userId);

        // Confirm recipe exists - exception will be thrown if not found
        getRecipe(userId, recipeId);

        recipeDao.save(updatedRecipe);

        return updatedRecipe;
    }

    public void deleteRecipe(UUID userId, UUID recipeId) {
        int numDeleted = recipeDao.deleteByIdAndUserId(recipeId, userId);
        if (numDeleted < 1) {
            String msg = "Recipe not found. Recipe ID: " + recipeId + ", User ID: " + userId;
            log.error(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }
    }

    public RecipesCategories categorizeRecipe(UUID recipeId, UUID categoryId) {
        RecipesCategories recipeCategory = new RecipesCategories(recipeId, categoryId);
        recipeCategoriesDao.save(recipeCategory);
        log.info("Saved recipe category: {}", recipeCategory);
        return recipeCategory;
    }

    public void uncategorizeRecipe(UUID recipeId, UUID categoryId) {
        int numDeleted = recipeCategoriesDao.delete(recipeId, categoryId);
        if (numDeleted < 1) {
            String msg = "Recipe category not found. Recipe ID: " + recipeId + ", Category ID: " + categoryId;
            log.error(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }
    }
}
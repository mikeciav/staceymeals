package com.ciav.staceymeals.service;

import com.ciav.staceymeals.model.FetchRecipeRequest;
import com.ciav.staceymeals.model.Recipe;
import com.ciav.staceymeals.model.UserRecipeDbEntry;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.threeten.extra.AmountFormats;
import org.threeten.extra.PeriodDuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class RecipeService {

    DynamoDbEnhancedClient db;

    private final DynamoDbTable<UserRecipeDbEntry> recipeTable;

    @Autowired
    public RecipeService (DynamoDbEnhancedClient db) {
        this.db = db;
        recipeTable = db.table("stacey-meals", TableSchema.fromBean(UserRecipeDbEntry.class));
    }

    public Recipe fetchAndSaveRecipe(FetchRecipeRequest request) {
        Recipe recipe = extractRecipe(request.getUrl());
        saveRecipe(recipe, request);
        return recipe;
    }

    public void saveRecipe(Recipe recipe, FetchRecipeRequest request) {
        UserRecipeDbEntry entry = UserRecipeDbEntry.builder()
                .userId(request.getUserId())
                .recipeId(recipe.getRecipeId())
                .recipe(recipe)
                .build();
        recipeTable.putItem(entry);
    }

    public Recipe extractRecipe(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; StaceyMealsBot/1.0)")
                    .timeout(10_000)
                    .get();

            //Try JSON-LD structured data
            Recipe recipe = parseJsonLdRecipe(doc, url);
            log.info("Extracted recipe using JSON-LD from URL: {}", url);
            return recipe;

        } catch (IOException e) {
            log.error("Error fetching URL {}: {}", url, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Parses application/ld+JSON blocks and looks for Recipe objects
    private Recipe parseJsonLdRecipe(Document doc, String sourceUrl) {
        Elements scripts = doc.select("script[type=application/ld+json]");
        for (Element script : scripts) {
            String json = script.html();
            // A simple regex-based approach to find Recipe blocks to avoid adding a JSON library
            // Look for "@type"\s*:\s*"Recipe" and then try to extract name, recipeIngredient, recipeInstructions
            if (json.contains("\"@type\"") && json.toLowerCase().contains("recipe")) {
                //String title = extractJsonField(json, "headline").orElse("");
                List<String> ingredients = extractJsonArrayField(json, "recipeIngredient");
                if (ingredients.isEmpty())
                    ingredients = extractJsonArrayField(json, "ingredients");
                // try to extract "text" occurrences inside recipeInstructions
                List<String> steps = extractJsonTextFromInstructions(json);
                String thumbnailUrl = extractJsonField(json, "thumbnailUrl").orElse("");

                String prepTime = extractTime(extractJsonField(json, "prepTime").orElse(""));
                String cookTime = extractTime(extractJsonField(json, "cookTime").orElse(""));
                String totalTime = extractTime(extractJsonField(json, "totalTime").orElse(""));
                String servings = extractJsonField(json, "recipeYield").orElse("0");

                return Recipe.builder()
                        .recipeId(UUID.randomUUID())
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
        throw new NotFoundException(msg);
    }

    // Very small JSON helpers using regex; not perfect but keeps deps minimal
    private Optional<String> extractJsonField(String json, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return Optional.of(m.group(1));
        return Optional.empty();
    }

    private List<String> extractJsonArrayField(String json, String field) {
        List<String> out = new ArrayList<>();
        // match "field": [ ... ] capturing inside the brackets
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (m.find()) {
            String inside = m.group(1);
            // extract quoted strings
            Pattern q = Pattern.compile("\\\"([^\\\"]+)\\\"");
            Matcher mq = q.matcher(inside);
            while (mq.find()) out.add(mq.group(1));
        }
        return out;
    }

    private List<String> extractJsonTextFromInstructions(String json) {
        List<String> out = new ArrayList<>();
        // look for "text": "..." occurrences
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
}
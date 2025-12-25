package com.ciav.staceymeals.service;

import com.ciav.staceymeals.model.Recipe;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class RecipeService {
    public Recipe extractRecipe(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; StaceyMealsBot/1.0)")
                    .timeout(10_000)
                    .get();

            // 1) Try JSON-LD structured data
            Optional<Recipe> jsonLd = parseJsonLdRecipe(doc);
            if (jsonLd.isPresent())
            {
                log.info("Extracted recipe using JSON-LD from URL: {}", url);
                return jsonLd.get();
            }

            // 2) Try microdata / itemprop attributes
            Optional<Recipe> microdata = parseMicrodataRecipe(doc);
            if (microdata.isPresent()) {
                log.info("Extracted recipe using Microdata from URL: {}", url);
                return microdata.get();
            }

            throw new IOException("No recipe data found in known formats.");

        } catch (IOException e) {
            log.error("Error fetching URL {}: {}", url, e.getMessage());
            List<String> empty = List.of();
            return Recipe.builder()
                    .title("Failed to fetch: " + url)
                    .ingredients(empty)
                    .steps(empty)
                    .build();
        }
    }

    // Parses application/ld+json blocks and looks for Recipe objects
    private Optional<Recipe> parseJsonLdRecipe(Document doc) {
        Elements scripts = doc.select("script[type=application/ld+json]");
        for (Element script : scripts) {
            String json = script.html();
            // A simple regex-based approach to find Recipe blocks to avoid adding a JSON library
            // Look for "@type"\s*:\s*"Recipe" and then try to extract name, recipeIngredient, recipeInstructions
            if (json.contains("\"@type\"") && json.toLowerCase().contains("recipe")) {
                String title = extractJsonField(json, "headline").orElse("");
                List<String> ingredients = extractJsonArrayField(json, "recipeIngredient");
                if (ingredients.isEmpty())
                    ingredients = extractJsonArrayField(json, "ingredients");
                // try to extract "text" occurrences inside recipeInstructions
                List<String> steps = extractJsonTextFromInstructions(json);
                String thumbnailUrl = extractJsonField(json, "thumbnailUrl").orElse("");

                String prepTime = extractTime(extractJsonField(json, "prepTime").orElse(""));
                String cookTime = extractTime(extractJsonField(json, "cookTime").orElse(""));
                String totalTime = extractTime(extractJsonField(json, "totalTime").orElse(""));

                return Optional.of(Recipe.builder()
                        .title(title == null || title.isEmpty() ? doc.title() : title)
                        .ingredients(ingredients)
                        .steps(steps)
                        .thumbnailUrl(thumbnailUrl)
                        .raw(json)
                        .build());
            }
        }
        return Optional.empty();
    }

    private Optional<Recipe> parseMicrodataRecipe(Document doc) {
        // itemprop-based extraction
        String title = firstText(doc.select("[itemprop=name]"));
        List<String> ingredients = elementsToTextList(doc.select("[itemprop=recipeIngredient], [itemprop=ingredients]"));
        List<String> steps = elementsToTextList(doc.select("[itemprop=recipeInstructions] [itemprop=text], [itemprop=recipeInstructions], [itemprop=step]"));

        if (!ingredients.isEmpty() || !steps.isEmpty() || (title != null && !title.isEmpty())) {
            return Optional.of(Recipe.builder()
                    .title((title == null || title.isEmpty()) ? doc.title() : title)
                    .ingredients(ingredients)
                    .steps(steps)
                    .raw(doc.toString())
                    .build());
        }
        return Optional.empty();
    }

    // Helper utilities
    private String firstText(Elements els) {
        if (els == null || els.isEmpty()) return null;
        return els.first().text();
    }

    private List<String> elementsToTextList(Elements els) {
        List<String> out = new ArrayList<>();
        if (els == null) return out;
        for (Element el : els) {
            String t = el.text().trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
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
            time = Duration.parse(isoTime).toString();
            return time;
        } catch (Exception e){
            log.warn("Failed to parse ISO 8601 time: {}", isoTime);
        }

        try {
            time = Period.parse(isoTime).toString();
            return time;
        }
        catch (Exception e){
            log.warn("Failed to parse ISO 8601 period: {}", isoTime);
        }

        log.error("Could not parse time via duration or period parsing: {}", isoTime);
        return "";
    }
}
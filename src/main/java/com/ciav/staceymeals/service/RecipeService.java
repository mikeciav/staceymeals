package com.ciav.staceymeals.service;

import com.ciav.staceymeals.model.Recipe;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.io.IOException;
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
            if (jsonLd.isPresent()) return jsonLd.get();

            // 2) Try microdata / itemprop attributes
            Optional<Recipe> microdata = parseMicrodataRecipe(doc);
            if (microdata.isPresent()) return microdata.get();

            // 3) Heuristic fallbacks: look for common classes/ids
            return parseHeuristics(doc, url);

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
                String title = extractJsonField(json, "name").orElse("");
                List<String> ingredients = extractJsonArrayField(json, "recipeIngredient");
                if (ingredients.isEmpty()) ingredients = extractJsonArrayField(json, "ingredients");
                List<String> steps = extractJsonArrayField(json, "recipeInstructions");
                // recipeInstructions is sometimes an array of objects with "text" fields
                if (steps.isEmpty()) {
                    // try to extract "text" occurrences inside recipeInstructions
                    steps = extractJsonTextFromInstructions(json);
                }
                return Optional.of(Recipe.builder()
                        .title(title == null || title.isEmpty() ? doc.title() : title)
                        .ingredients(ingredients)
                        .steps(steps)
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
                    .build());
        }
        return Optional.empty();
    }

    private Recipe parseHeuristics(Document doc, String url) {
        String title = doc.title();
        List<String> ingredients = new ArrayList<>();
        List<String> steps = new ArrayList<>();

        // Common selectors
        Elements ingEls = doc.select(".ingredients, .recipe-ingredients, #ingredients, [class*=ingredient]");
        for (Element el : ingEls) {
            // flatten lists and paragraphs
            ingredients.addAll(elementsToTextList(el.select("li, p, div")));
            if (ingredients.isEmpty()) ingredients.addAll(elementsToTextList(el.select("*")));
        }

        Elements stepEls = doc.select(".instructions, .directions, .method, .preparation, #instructions, [class*=instruction], [class*=direction], [class*=method]");
        for (Element el : stepEls) {
            steps.addAll(elementsToTextList(el.select("li, p, div")));
            if (steps.isEmpty()) steps.addAll(elementsToTextList(el.select("*")));
        }

        // last resort: look for long paragraphs and split by sentence-ish boundaries
        if (ingredients.isEmpty()) {
            Elements paras = doc.select("p");
            for (Element p : paras) {
                if (p.text().toLowerCase().contains("cup") || p.text().toLowerCase().matches(".*\\d+.*")) {
                    ingredients.add(p.text());
                }
            }
        }

        if (steps.isEmpty()) {
            Elements paras = doc.select("p");
            for (Element p : paras) {
                String t = p.text();
                if (t.toLowerCase().startsWith("preheat") || t.toLowerCase().contains("minutes") || t.toLowerCase().contains("cook")) {
                    // split into sentences
                    for (String s : t.split("(?<=[.!?])\\s+")) {
                        steps.add(s.trim());
                    }
                }
            }
        }

        return Recipe.builder()
                .title(title == null || title.isEmpty() ? url : title)
                .ingredients(ingredients)
                .steps(steps)
                .build();
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
}
package com.ciav.staceymeals.db.mapper;

import com.ciav.staceymeals.model.Category;
import com.ciav.staceymeals.model.Recipe;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;

import java.util.Map;
import java.util.UUID;

public class RecipeRowMapper implements LinkedHashMapRowReducer<UUID, Recipe> {

    @Override
    public void accumulate(Map<UUID, Recipe> map, RowView rowView) {
        Recipe recipe = map.computeIfAbsent(
            rowView.getColumn("id", UUID.class),
            id -> rowView.getRow(Recipe.class)
        );

        UUID categoryId = rowView.getColumn("category_id", UUID.class);
        if (categoryId != null) {
            Category category = Category.builder()
                .id(rowView.getColumn("category_id", UUID.class))
                .name(rowView.getColumn("category_name", String.class))
                .build();
            recipe.getCategories().add(category);
        }
    }
}
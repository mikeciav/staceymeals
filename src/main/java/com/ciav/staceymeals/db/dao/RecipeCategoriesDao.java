package com.ciav.staceymeals.db.dao;

import com.ciav.staceymeals.model.RecipesCategories;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

@RegisterBeanMapper(RecipesCategories.class)
public interface RecipeCategoriesDao {

    default UUID save(@BindBean RecipesCategories recipesCategories){
        return saveBatch(List.of(recipesCategories)).get(0);
    }

    @SqlBatch("""
            INSERT INTO recipes_categories (recipe_id, category_id)
            VALUES (:recipeId, :categoryId)
            ON CONFLICT (recipe_id, category_id) DO NOTHING
            """)
    @GetGeneratedKeys
    List<UUID> saveBatch(@BindBean List<RecipesCategories> recipesCategories);

    @SqlUpdate("""
            DELETE FROM recipes_categories
            WHERE recipe_id = :recipeId AND category_id = :categoryId
            """)
    int delete(@Bind("recipeId") UUID recipeId, @Bind("categoryId") UUID categoryId);
}
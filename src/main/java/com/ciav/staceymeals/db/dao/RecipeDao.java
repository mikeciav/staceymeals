package com.ciav.staceymeals.db.dao;

import com.ciav.staceymeals.db.mapper.RecipeRowMapper;
import com.ciav.staceymeals.model.Recipe;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RegisterBeanMapper(Recipe.class)
public interface RecipeDao {

    @SqlUpdate("""
            INSERT INTO recipes (id, user_id, source_url, title, ingredients, steps,
                                thumbnail_url, prep_time, cook_time, total_time, servings, raw, rating)
            VALUES (:id, :userId, :sourceUrl, :title, :ingredients, :steps,
                    :thumbnailUrl, :prepTime, :cookTime, :totalTime, :servings, :raw, :rating)
            ON CONFLICT (id) DO UPDATE SET
                user_id = EXCLUDED.user_id,
                source_url = EXCLUDED.source_url,
                title = EXCLUDED.title,
                ingredients = EXCLUDED.ingredients,
                steps = EXCLUDED.steps,
                thumbnail_url = EXCLUDED.thumbnail_url,
                prep_time = EXCLUDED.prep_time,
                cook_time = EXCLUDED.cook_time,
                total_time = EXCLUDED.total_time,
                servings = EXCLUDED.servings,
                raw = EXCLUDED.raw,
                rating = EXCLUDED.rating
            returning *
            """)
    @GetGeneratedKeys
    UUID save(@BindBean Recipe recipe);

    @SqlQuery("""
            SELECT r.*, c.id as category_id, c.name as category_name
            FROM recipes r
            LEFT JOIN recipes_categories rc ON r.id = rc.recipe_id
            LEFT JOIN categories c ON rc.category_id = c.id
            WHERE r.user_id = :userId
            ORDER BY r.id, c.id
            """)
    @UseRowReducer(RecipeRowMapper.class)
    List<Recipe> findByUserId(@Bind("userId") UUID userId);

    @SqlQuery("""
            SELECT r.*, c.id as category_id, c.name as category_name
            FROM recipes r
            LEFT JOIN recipes_categories rc ON r.id = rc.recipe_id
            LEFT JOIN categories c ON rc.category_id = c.id
            WHERE r.id = :id AND r.user_id = :userId
            ORDER BY c.id
            """)
    @UseRowReducer(RecipeRowMapper.class)
    Optional<Recipe> findByIdAndUserId(@Bind("id") UUID id, @Bind("userId") UUID userId);

    @SqlUpdate("DELETE FROM recipes WHERE id = :id AND user_id = :userId")
    int deleteByIdAndUserId(@Bind("id") UUID id, @Bind("userId") UUID userId);
}
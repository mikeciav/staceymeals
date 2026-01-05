package com.ciav.staceymeals.db.dao;

import com.ciav.staceymeals.model.Category;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RegisterBeanMapper(Category.class)
public interface CategoryDao {

    @SqlUpdate("""
            INSERT INTO categories (id, user_id, name, parent_category_id)
            VALUES (:id, :userId, :name, :parentCategoryId)
            ON CONFLICT (id) DO UPDATE SET
                user_id = EXCLUDED.user_id,
                name = EXCLUDED.name,
                parent_category_id = EXCLUDED.parent_category_id
            """)
    @GetGeneratedKeys("id")
    UUID save(@BindBean Category category);

    @SqlQuery("SELECT * FROM categories WHERE id = :id AND user_id = :userId")
    Optional<Category> findByIdAndUserId(@Bind("id") UUID id, @Bind("userId") UUID userId);

    @SqlQuery("SELECT * FROM categories WHERE user_id = :userId ORDER BY name")
    List<Category> findByUserId(@Bind("userId") UUID userId);
}
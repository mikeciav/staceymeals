package com.ciav.staceymeals.config;

import com.ciav.staceymeals.db.dao.CategoryDao;
import com.ciav.staceymeals.db.dao.RecipeCategoriesDao;
import com.ciav.staceymeals.db.dao.RecipeDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JdbiConfiguration {

    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource)
                .installPlugin(new SqlObjectPlugin())
                .installPlugin(new PostgresPlugin());
    }

    @Bean
    public RecipeDao recipeDao(Jdbi jdbi) {
        return jdbi.onDemand(RecipeDao.class);
    }

    @Bean
    public CategoryDao categoryDao(Jdbi jdbi) {
        return jdbi.onDemand(CategoryDao.class);
    }

    @Bean
    public RecipeCategoriesDao recipeCategoriesDao(Jdbi jdbi) {
        return jdbi.onDemand(RecipeCategoriesDao.class);
    }
}
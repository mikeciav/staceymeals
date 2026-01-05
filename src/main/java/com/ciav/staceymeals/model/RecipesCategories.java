package com.ciav.staceymeals.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Builder
@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Table("recipes_categories")
public class RecipesCategories {
	private UUID recipeId;
	private UUID categoryId;
}

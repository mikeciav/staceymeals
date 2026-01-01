package com.ciav.staceymeals.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@Slf4j
@DynamoDbBean
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
	private UUID recipeId;
	private String sourceUrl;
	private String title;
	private List<String> ingredients;
	private List<String> steps;
	private String thumbnailUrl;
	private String prepTime;
	private String cookTime;
	private String totalTime;
	private String servings;
	private String raw;


}

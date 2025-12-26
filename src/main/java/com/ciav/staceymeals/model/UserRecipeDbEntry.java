package com.ciav.staceymeals.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.UUID;

@DynamoDbBean
@Builder
@Slf4j
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRecipeDbEntry {
	private String userId;
	private UUID recipeId;
	@Getter
	private Recipe recipe;

	@DynamoDbPartitionKey
	public String getUserId() {
		return userId;
	}

	@DynamoDbSortKey
	public UUID getRecipeId() {
		return recipeId;
	}

}

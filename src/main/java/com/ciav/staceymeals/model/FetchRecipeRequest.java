package com.ciav.staceymeals.model;

import lombok.Data;
import software.amazon.awssdk.annotations.NotNull;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.UUID;

@DynamoDbBean
@Data
public class FetchRecipeRequest {
	@NotNull
	private String userId;
	@NotNull
	private String url;
}

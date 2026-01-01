package com.ciav.staceymeals.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.UUID;

@DynamoDbBean
@SuperBuilder
@Slf4j
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class DynamoDbRecord {
	protected String userId;
	protected String sortKey;

	@DynamoDbPartitionKey
	public String getUserId() {
		return userId;
	}

	@DynamoDbSortKey
	public String getSortKey() {
		return sortKey;
	}

}

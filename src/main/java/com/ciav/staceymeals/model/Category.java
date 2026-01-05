package com.ciav.staceymeals.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
public class Category {
	@Id
	private UUID id;
	private UUID userId;
	private String name;
	private UUID parentCategoryId;
	@Builder.Default
	@Transient
	private List<Category> subCategories = new ArrayList<>();
}

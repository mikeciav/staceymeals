package com.ciav.staceymeals.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Builder
@Data
@Slf4j
public class Recipe {

	private String title;
	private List<String> ingredients;
	private List<String> steps;
	private String thumbnailUrl;
	private String raw;


}

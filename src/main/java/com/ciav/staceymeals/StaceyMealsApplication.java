package com.ciav.staceymeals;

import com.ciav.staceymeals.controller.RecipeController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ RecipeController.class })
public class StaceyMealsApplication {
	public static void main(String[] args) {
		SpringApplication.run(StaceyMealsApplication.class, args);
	}

}

package com.ciav.staceymeals;

import com.ciav.staceymeals.controller.RecipeController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ RecipeController.class })
public class StaceyMealsApplication {
	public static void main(String[] args) {
		System.out.println("DB_HOST: " + System.getenv("DB_HOST"));
		System.out.println("DB_USER: " + System.getenv("DB_USER"));
		System.out.println("DB_PASSWORD: " + System.getenv("DB_PASSWORD"));
		SpringApplication.run(StaceyMealsApplication.class, args);
	}

}

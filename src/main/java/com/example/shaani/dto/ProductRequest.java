package com.example.shaani.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductRequest(
		@NotBlank String name,
		String slug,
		@NotNull @Positive Integer price,
		@NotNull @Positive Integer originalPrice,
		@NotBlank String category,
		String occasion,
		String fabric,
		List<String> sizes,
		String badge,
		@NotBlank String image,
		String alt,
		List<String> tags,
		Boolean stock,
		String description) {
}

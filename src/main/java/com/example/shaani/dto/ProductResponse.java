package com.example.shaani.dto;

import java.util.List;

public record ProductResponse(
		String id,
		String slug,
		String name,
		Integer price,
		Integer originalPrice,
		String category,
		String categorySlug,
		String occasion,
		String fabric,
		List<String> sizes,
		String badge,
		String image,
		List<String> images,
		String alt,
		List<String> tags,
		boolean stock,
		String description) {
}

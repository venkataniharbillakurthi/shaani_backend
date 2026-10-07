package com.example.shaani.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
		String productId,
		@NotBlank String title,
		@NotBlank String size,
		@NotNull @Min(1) Integer qty,
		@NotNull @Min(1) Integer price,
		Integer originalPrice,
		String image) {
}

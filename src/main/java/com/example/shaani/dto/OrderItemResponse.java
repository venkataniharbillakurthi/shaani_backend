package com.example.shaani.dto;

public record OrderItemResponse(
		String productId,
		String title,
		String size,
		int qty,
		int price,
		int originalPrice,
		String image,
		int lineTotal) {
}

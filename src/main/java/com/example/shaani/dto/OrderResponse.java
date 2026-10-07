package com.example.shaani.dto;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
		Long id,
		String status,
		Instant createdAt,
		Instant processedAt,
		String name,
		String phone,
		String email,
		String pincode,
		String address,
		String locality,
		String landmark,
		String city,
		String state,
		String note,
		String cancelReason,
		Instant cancelledAt,
		int subtotal,
		boolean countsTowardRevenue,
		List<OrderItemResponse> items) {
}

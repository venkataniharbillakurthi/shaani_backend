package com.example.shaani.dto;

import java.time.Instant;

public record ContactResponse(
		Long id,
		Instant createdAt,
		String name,
		String phone,
		String email,
		String subject,
		String message) {
}

package com.example.shaani.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
		@NotBlank String name,
		@NotBlank String phone,
		@NotBlank @Email String email,
		@NotBlank String subject,
		@NotBlank @Size(max = 2000) String message) {
}

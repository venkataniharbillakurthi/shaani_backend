package com.example.shaani.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
		@NotBlank String name,
		@NotBlank String phone,
		@NotBlank @Email String email,
		@NotBlank String pincode,
		@NotBlank String address,
		@NotBlank String locality,
		String landmark,
		@NotBlank String city,
		@NotBlank String state,
		String note) {
}

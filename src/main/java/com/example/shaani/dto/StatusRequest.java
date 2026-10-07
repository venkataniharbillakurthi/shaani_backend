package com.example.shaani.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusRequest(@NotBlank String status, String reason) {
}

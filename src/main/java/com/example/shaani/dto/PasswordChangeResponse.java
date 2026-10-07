package com.example.shaani.dto;

public record PasswordChangeResponse(String username, boolean emailed, String message) {
}

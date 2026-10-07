package com.example.shaani.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.shaani.dto.AuthResponse;
import com.example.shaani.dto.LoginRequest;
import com.example.shaani.dto.PasswordChangeRequest;
import com.example.shaani.dto.PasswordChangeResponse;
import com.example.shaani.service.AdminAuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AdminAuthService adminAuthService;

	public AuthController(AdminAuthService adminAuthService) {
		this.adminAuthService = adminAuthService;
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return adminAuthService.login(request.username(), request.password());
	}

	@GetMapping("/me")
	public Map<String, String> me(Authentication authentication) {
		return Map.of("username", authentication.getName());
	}

	@PutMapping("/password")
	public PasswordChangeResponse changePassword(Authentication authentication, @Valid @RequestBody PasswordChangeRequest request) {
		return adminAuthService.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
	}
}

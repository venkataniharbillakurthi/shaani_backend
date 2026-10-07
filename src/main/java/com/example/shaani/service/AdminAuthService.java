package com.example.shaani.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.shaani.dto.AuthResponse;
import com.example.shaani.dto.PasswordChangeResponse;
import com.example.shaani.entity.AdminUser;
import com.example.shaani.error.ApiException;
import com.example.shaani.repository.AdminUserRepository;
import com.example.shaani.security.JwtService;

@Service
public class AdminAuthService {

	private final AdminUserRepository adminUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final PasswordMailService passwordMailService;

	public AdminAuthService(
			AdminUserRepository adminUserRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			PasswordMailService passwordMailService) {
		this.adminUserRepository = adminUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.passwordMailService = passwordMailService;
	}

	public AuthResponse login(String username, String password) {
		AdminUser admin = adminUserRepository.findByUsername(username.trim())
				.filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect."));
		return new AuthResponse(jwtService.createToken(admin.getUsername()), admin.getUsername());
	}

	public PasswordChangeResponse changePassword(String username, String currentPassword, String newPassword) {
		AdminUser admin = adminUserRepository.findByUsername(username)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
		if (!passwordEncoder.matches(currentPassword, admin.getPasswordHash())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
		}
		if (currentPassword.equals(newPassword)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a different password.");
		}
		admin.setPasswordHash(passwordEncoder.encode(newPassword));
		adminUserRepository.save(admin);
		boolean emailed = passwordMailService.sendPassword(admin.getUsername(), newPassword);
		String message = emailed
				? "Password updated and sent to venkataniharbillakurthi@gmail.com."
				: "Password updated. Email was not sent. Add a Gmail app password in MAIL_PASSWORD and try again.";
		return new PasswordChangeResponse(admin.getUsername(), emailed, message);
	}
}

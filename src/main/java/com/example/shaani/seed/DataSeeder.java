package com.example.shaani.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.shaani.entity.AdminUser;
import com.example.shaani.repository.AdminUserRepository;
import com.example.shaani.service.PasswordMailService;
import com.example.shaani.service.ProductService;

@Component
public class DataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final AdminUserRepository adminUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordMailService passwordMailService;
	private final ProductService productService;
	private final String adminUsername;
	private final String initialPassword;

	public DataSeeder(
			AdminUserRepository adminUserRepository,
			PasswordEncoder passwordEncoder,
			PasswordMailService passwordMailService,
			ProductService productService,
			@Value("${shaani.admin.username}") String adminUsername,
			@Value("${shaani.admin.initial-password}") String initialPassword) {
		this.adminUserRepository = adminUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.passwordMailService = passwordMailService;
		this.productService = productService;
		this.adminUsername = adminUsername;
		this.initialPassword = initialPassword;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (adminUserRepository.count() == 0) {
			AdminUser admin = new AdminUser();
			admin.setUsername(adminUsername);
			admin.setPasswordHash(passwordEncoder.encode(initialPassword));
			adminUserRepository.save(admin);
			boolean emailed = passwordMailService.sendPassword(adminUsername, initialPassword);
			log.info("Admin user created. Password email sent: {}", emailed);
		}
		productService.normalizeCatalogue();
	}
}

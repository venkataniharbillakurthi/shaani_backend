package com.example.shaani.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordMailService {

	private static final Logger log = LoggerFactory.getLogger(PasswordMailService.class);

	private final JavaMailSender mailSender;
	private final String from;
	private final String notifyEmail;
	private final String mailPassword;

	public PasswordMailService(
			JavaMailSender mailSender,
			@Value("${spring.mail.username}") String from,
			@Value("${shaani.admin.notify-email}") String notifyEmail,
			@Value("${spring.mail.password:}") String mailPassword) {
		this.mailSender = mailSender;
		this.from = from;
		this.notifyEmail = notifyEmail;
		this.mailPassword = mailPassword;
	}

	public boolean sendPassword(String username, String password) {
		if (mailPassword == null || mailPassword.isBlank()) {
			log.warn("Admin password was not emailed. Set MAIL_PASSWORD to a Gmail app password.");
			return false;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(notifyEmail);
			message.setSubject("Shaani Clothing admin password");
			message.setText("Shaani Clothing admin sign-in\n\nUsername: " + username + "\nPassword: " + password
					+ "\n\nSign in at /admin and change this password if you did not just set it.");
			mailSender.send(message);
			return true;
		} catch (RuntimeException exception) {
			log.warn("Could not email the admin password: {}", exception.getMessage());
			return false;
		}
	}
}

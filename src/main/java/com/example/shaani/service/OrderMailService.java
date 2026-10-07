package com.example.shaani.service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.shaani.entity.ContactMessage;
import com.example.shaani.entity.OrderItem;
import com.example.shaani.entity.ShopOrder;

@Service
public class OrderMailService {

	private static final Logger log = LoggerFactory.getLogger(OrderMailService.class);
	private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")
			.withZone(ZoneId.of("Asia/Kolkata"));

	private final JavaMailSender mailSender;
	private final String from;
	private final String notifyEmail;
	private final String mailPassword;

	public OrderMailService(
			JavaMailSender mailSender,
			@Value("${spring.mail.username}") String from,
			@Value("${shaani.admin.notify-email}") String notifyEmail,
			@Value("${spring.mail.password:}") String mailPassword) {
		this.mailSender = mailSender;
		this.from = from;
		this.notifyEmail = notifyEmail;
		this.mailPassword = mailPassword;
	}

	public void notifyPlaced(ShopOrder order) {
		send("New Shaani order " + order.getId(), order, null);
	}

	public void notifyStatus(ShopOrder order, String previousStatus) {
		send("Shaani order " + order.getId() + " is now " + order.getStatus().name().toLowerCase(), order, previousStatus);
	}

	public void notifyContact(ContactMessage message) {
		if (mailPassword == null || mailPassword.isBlank()) {
			log.warn("Contact email was not sent. Gmail app password is missing.");
			return;
		}
		try {
			SimpleMailMessage mail = new SimpleMailMessage();
			mail.setFrom(from);
			mail.setTo(notifyEmail);
			mail.setReplyTo(message.getEmail());
			mail.setSubject("Shaani contact: " + message.getSubject());
			mail.setText("New message from the contact page\n\n"
					+ "Name: " + message.getName() + "\n"
					+ "Mobile: " + message.getPhone() + "\n"
					+ "Email: " + message.getEmail() + "\n"
					+ "Subject: " + message.getSubject() + "\n\n"
					+ message.getMessage() + "\n");
			mailSender.send(mail);
			log.info("Contact email sent for message {}", message.getId());
		} catch (RuntimeException exception) {
			log.warn("Could not email contact message {}: {}", message.getId(), exception.getMessage());
		}
	}

	private void send(String subject, ShopOrder order, String previousStatus) {
		if (mailPassword == null || mailPassword.isBlank()) {
			log.warn("Order email was not sent. Gmail app password is missing.");
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(notifyEmail);
			message.setSubject(subject);
			message.setText(body(order, previousStatus));
			mailSender.send(message);
			log.info("Order email sent for order {}", order.getId());
		} catch (RuntimeException exception) {
			log.warn("Could not email order {}: {}", order.getId(), exception.getMessage());
		}
	}

	private String body(ShopOrder order, String previousStatus) {
		StringBuilder text = new StringBuilder();
		text.append("Shaani Clothing order ").append(order.getId()).append("\n\n");
		text.append("Status: ").append(order.getStatus().name().toLowerCase()).append("\n");
		if (previousStatus != null) {
			text.append("Previous status: ").append(previousStatus).append("\n");
		}
		if (order.getCreatedAt() != null) {
			text.append("Placed: ").append(WHEN.format(order.getCreatedAt())).append(" IST\n");
		}
		text.append("\nCustomer: ").append(order.getCustomerName()).append("\n");
		text.append("Mobile: ").append(order.getPhone()).append("\n");
		text.append("Email: ").append(order.getEmail()).append("\n");
		text.append("Address: ").append(order.getAddress()).append("\n");
		text.append(order.getLocality());
		if (order.getLandmark() != null && !order.getLandmark().isBlank()) {
			text.append(", ").append(order.getLandmark());
		}
		text.append("\n").append(order.getCity()).append(", ").append(order.getState()).append(" ").append(order.getPincode()).append("\n");
		if (order.getNote() != null && !order.getNote().isBlank()) {
			text.append("Note: ").append(order.getNote()).append("\n");
		}
		if (order.getCancelReason() != null && !order.getCancelReason().isBlank()) {
			text.append("Cancellation reason: ").append(order.getCancelReason()).append("\n");
		}
		text.append("\nProducts:\n");
		for (OrderItem item : order.getItems()) {
			text.append("- ").append(item.getTitle())
					.append(", size ").append(item.getSizeLabel())
					.append(", qty ").append(item.getQty())
					.append(", ₹").append(item.getPrice() * item.getQty())
					.append("\n");
		}
		text.append("\nTotal: ₹").append(order.getSubtotal()).append("\n");
		return text.toString();
	}
}

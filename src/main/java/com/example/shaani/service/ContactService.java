package com.example.shaani.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shaani.dto.ContactRequest;
import com.example.shaani.dto.ContactResponse;
import com.example.shaani.entity.ContactMessage;
import com.example.shaani.error.ApiException;
import com.example.shaani.repository.ContactMessageRepository;

@Service
@Transactional
public class ContactService {

	private final ContactMessageRepository contactMessageRepository;
	private final OrderMailService orderMailService;

	public ContactService(ContactMessageRepository contactMessageRepository, OrderMailService orderMailService) {
		this.contactMessageRepository = contactMessageRepository;
		this.orderMailService = orderMailService;
	}

	public ContactResponse save(ContactRequest request) {
		String phone = request.phone().replaceAll("\\s+", "").replaceFirst("^\\+91", "");
		if (!phone.matches("[6-9]\\d{9}")) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a 10-digit Indian mobile number.");
		}
		ContactMessage message = new ContactMessage();
		message.setCreatedAt(Instant.now());
		message.setName(request.name().trim());
		message.setPhone(phone);
		message.setEmail(request.email().trim());
		message.setSubject(request.subject().trim());
		message.setMessage(request.message().trim());
		ContactMessage saved = contactMessageRepository.save(message);
		orderMailService.notifyContact(saved);
		return toResponse(saved);
	}

	public List<ContactResponse> list() {
		return contactMessageRepository.findAll().stream()
				.sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
				.map(this::toResponse)
				.toList();
	}

	public void delete(Long id) {
		if (!contactMessageRepository.existsById(id)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Message not found.");
		}
		contactMessageRepository.deleteById(id);
	}

	private ContactResponse toResponse(ContactMessage message) {
		return new ContactResponse(
				message.getId(),
				message.getCreatedAt(),
				message.getName(),
				message.getPhone(),
				message.getEmail(),
				message.getSubject(),
				message.getMessage());
	}
}

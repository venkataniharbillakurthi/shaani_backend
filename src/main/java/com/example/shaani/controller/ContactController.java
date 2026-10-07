package com.example.shaani.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.shaani.dto.ContactRequest;
import com.example.shaani.dto.ContactResponse;
import com.example.shaani.service.ContactService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
public class ContactController {

	private final ContactService contactService;

	public ContactController(ContactService contactService) {
		this.contactService = contactService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ContactResponse create(@Valid @RequestBody ContactRequest request) {
		return contactService.save(request);
	}
}

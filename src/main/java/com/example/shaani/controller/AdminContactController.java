package com.example.shaani.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.shaani.dto.ContactResponse;
import com.example.shaani.service.ContactService;

@RestController
@RequestMapping("/api/admin/messages")
public class AdminContactController {

	private final ContactService contactService;

	public AdminContactController(ContactService contactService) {
		this.contactService = contactService;
	}

	@GetMapping
	public List<ContactResponse> list() {
		return contactService.list();
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		contactService.delete(id);
	}
}

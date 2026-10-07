package com.example.shaani.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.shaani.service.SpacesStorageService;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

	private final SpacesStorageService spacesStorageService;

	public AdminUploadController(SpacesStorageService spacesStorageService) {
		this.spacesStorageService = spacesStorageService;
	}

	@PostMapping
	public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
		return Map.of("url", spacesStorageService.upload(file));
	}
}

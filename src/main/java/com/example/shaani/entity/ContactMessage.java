package com.example.shaani.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
public class ContactMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String subject;

	@Column(nullable = false, length = 2000)
	private String message;
}

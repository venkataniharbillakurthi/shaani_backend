package com.example.shaani.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shaani.entity.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}

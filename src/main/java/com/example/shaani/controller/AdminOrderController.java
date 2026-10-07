package com.example.shaani.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.shaani.dto.OrderResponse;
import com.example.shaani.dto.RevenueResponse;
import com.example.shaani.dto.StatusRequest;
import com.example.shaani.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminOrderController {

	private final OrderService orderService;

	public AdminOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/orders")
	public List<OrderResponse> list() {
		return orderService.list();
	}

	@PutMapping("/orders/{id}/status")
	public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
		return orderService.updateStatus(id, request.status(), request.reason());
	}

	@DeleteMapping("/orders/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		orderService.delete(id);
	}

	@GetMapping("/revenue")
	public RevenueResponse revenue(@RequestParam(defaultValue = "day") String range, @RequestParam(required = false) String date) {
		return orderService.revenue(range, date);
	}
}

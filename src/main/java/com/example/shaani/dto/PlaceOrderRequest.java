package com.example.shaani.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
		@NotNull @Valid CustomerRequest customer,
		@NotEmpty List<@Valid OrderItemRequest> items) {
}

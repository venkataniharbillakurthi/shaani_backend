package com.example.shaani.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shaani.dto.OrderItemResponse;
import com.example.shaani.dto.OrderResponse;
import com.example.shaani.dto.PlaceOrderRequest;
import com.example.shaani.dto.RevenueResponse;
import com.example.shaani.entity.OrderItem;
import com.example.shaani.entity.OrderStatus;
import com.example.shaani.entity.ShopOrder;
import com.example.shaani.error.ApiException;
import com.example.shaani.repository.OrderRepository;

@Service
@Transactional
public class OrderService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
	private static final Set<OrderStatus> REVENUE_STATUSES = EnumSet.of(OrderStatus.PROCESSING, OrderStatus.DELIVERED);

	private final OrderRepository orderRepository;
	private final OrderMailService orderMailService;

	public OrderService(OrderRepository orderRepository, OrderMailService orderMailService) {
		this.orderRepository = orderRepository;
		this.orderMailService = orderMailService;
	}

	public OrderResponse place(PlaceOrderRequest request) {
		ShopOrder order = new ShopOrder();
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(Instant.now());
		order.setCustomerName(request.customer().name().trim());
		order.setPhone(request.customer().phone().trim());
		order.setEmail(request.customer().email().trim());
		order.setPincode(request.customer().pincode().trim());
		order.setAddress(request.customer().address().trim());
		order.setLocality(request.customer().locality().trim());
		order.setLandmark(blank(request.customer().landmark()));
		order.setCity(request.customer().city().trim());
		order.setState(request.customer().state().trim());
		order.setNote(blank(request.customer().note()));
		int subtotal = 0;
		for (var item : request.items()) {
			OrderItem line = new OrderItem();
			line.setProductId(item.productId());
			line.setTitle(item.title().trim());
			line.setSizeLabel(item.size().trim());
			line.setQty(item.qty());
			line.setPrice(item.price());
			line.setOriginalPrice(item.originalPrice() == null ? item.price() : item.originalPrice());
			line.setImage(item.image() == null ? "" : item.image());
			order.getItems().add(line);
			subtotal += item.price() * item.qty();
		}
		order.setSubtotal(subtotal);
		ShopOrder saved = orderRepository.save(order);
		orderMailService.notifyPlaced(saved);
		return toResponse(saved);
	}

	public List<OrderResponse> list() {
		return orderRepository.findAll().stream()
				.sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
				.map(this::toResponse)
				.toList();
	}

	public OrderResponse updateStatus(Long id, String rawStatus, String reason) {
		ShopOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));
		OrderStatus next = parseStatus(rawStatus);
		String previousStatus = order.getStatus().name().toLowerCase();
		if (!allowed(order.getStatus(), next)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "This status change is not allowed.");
		}
		if (next == OrderStatus.CANCELLED) {
			String cancelReason = reason == null ? "" : reason.trim();
			if (cancelReason.isBlank()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "A cancellation reason is required.");
			}
			order.setCancelReason(cancelReason);
			order.setCancelledAt(Instant.now());
		}
		if (next == OrderStatus.PENDING) {
			order.setProcessedAt(null);
		}
		if ((next == OrderStatus.PROCESSING || next == OrderStatus.DELIVERED) && order.getProcessedAt() == null) {
			order.setProcessedAt(Instant.now());
		}
		order.setStatus(next);
		ShopOrder saved = orderRepository.save(order);
		orderMailService.notifyStatus(saved, previousStatus);
		return toResponse(saved);
	}

	public void delete(Long id) {
		ShopOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));
		orderRepository.delete(order);
	}

	public RevenueResponse revenue(String range, String date) {
		String mode = range == null || range.isBlank() ? "day" : range.trim().toLowerCase();
		LocalDate today = LocalDate.now(ZONE);
		Instant start;
		Instant end;
		String label;
		if ("month".equals(mode)) {
			YearMonth month = date == null || date.isBlank() ? YearMonth.from(today) : YearMonth.parse(date);
			start = month.atDay(1).atStartOfDay(ZONE).toInstant();
			end = month.plusMonths(1).atDay(1).atStartOfDay(ZONE).toInstant();
			label = month.toString();
			mode = "month";
		} else {
			LocalDate day = date == null || date.isBlank() ? today : LocalDate.parse(date);
			start = day.atStartOfDay(ZONE).toInstant();
			end = day.plusDays(1).atStartOfDay(ZONE).toInstant();
			label = day.toString();
			mode = "day";
		}
		long total = orderRepository.sumRevenue(REVENUE_STATUSES, start, end);
		long orders = orderRepository.countRevenueOrders(REVENUE_STATUSES, start, end);
		return new RevenueResponse(mode, label, total, orders);
	}

	private boolean allowed(OrderStatus current, OrderStatus next) {
		if (current == next || current == OrderStatus.CANCELLED) {
			return false;
		}
		return true;
	}

	private OrderStatus parseStatus(String rawStatus) {
		try {
			return OrderStatus.valueOf(rawStatus.trim().toUpperCase());
		} catch (RuntimeException exception) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Choose pending, processing, delivered, or cancelled.");
		}
	}

	private OrderResponse toResponse(ShopOrder order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(item -> new OrderItemResponse(
						item.getProductId(),
						item.getTitle(),
						item.getSizeLabel(),
						item.getQty(),
						item.getPrice(),
						item.getOriginalPrice(),
						item.getImage(),
						item.getPrice() * item.getQty()))
				.toList();
		boolean counts = REVENUE_STATUSES.contains(order.getStatus());
		return new OrderResponse(
				order.getId(),
				order.getStatus().name().toLowerCase(),
				order.getCreatedAt(),
				order.getProcessedAt(),
				order.getCustomerName(),
				order.getPhone(),
				order.getEmail(),
				order.getPincode(),
				order.getAddress(),
				order.getLocality(),
				order.getLandmark(),
				order.getCity(),
				order.getState(),
				order.getNote(),
				order.getCancelReason(),
				order.getCancelledAt(),
				order.getSubtotal(),
				counts,
				items);
	}

	private String blank(String value) {
		return value == null ? "" : value.trim();
	}
}

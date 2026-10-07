package com.example.shaani.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shop_orders")
@Getter
@Setter
@NoArgsConstructor
public class ShopOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status = OrderStatus.PENDING;

	@Column(nullable = false)
	private Instant createdAt;

	private Instant processedAt;

	@Column(nullable = false)
	private String customerName;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false, length = 6)
	private String pincode;

	@Column(nullable = false, length = 500)
	private String address;

	@Column(nullable = false)
	private String locality;

	private String landmark;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String state;

	@Column(length = 500)
	private String note;

	@Column(length = 500)
	private String cancelReason;

	private Instant cancelledAt;

	@Column(nullable = false)
	private int subtotal;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "order_id")
	private List<OrderItem> items = new ArrayList<>();
}

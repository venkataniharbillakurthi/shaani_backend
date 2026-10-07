package com.example.shaani.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

	@Id
	@Column(length = 80)
	private String id;

	@Column(nullable = false, unique = true, length = 80)
	private String slug;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer originalPrice;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false, length = 80)
	private String categorySlug;

	private String occasion;

	private String fabric;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
	@Column(name = "size_label", length = 20)
	private List<String> sizes = new ArrayList<>();

	private String badge;

	@Column(nullable = false, length = 2048)
	private String image;

	@Column(length = 300)
	private String altText;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
	@Column(name = "tag", length = 40)
	private List<String> tags = new ArrayList<>();

	@Column(nullable = false)
	private boolean stock = true;

	@Column(length = 2000)
	private String description;
}

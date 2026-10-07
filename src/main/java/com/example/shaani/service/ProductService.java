package com.example.shaani.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.shaani.dto.ProductRequest;
import com.example.shaani.dto.ProductResponse;
import com.example.shaani.entity.Product;
import com.example.shaani.error.ApiException;
import com.example.shaani.repository.ProductRepository;

@Service
public class ProductService {

	public static final Set<String> TAGS = Set.of("new-arrival", "curated");

	private static final Map<String, String> CATEGORY_SLUG = Map.ofEntries(
			Map.entry("New Collections", "new-collections"),
			Map.entry("Dress Materials", "dress-materials"),
			Map.entry("3 Piece Sets", "3-piece-sets"),
			Map.entry("Palazzo Pants", "palazzo-pants"),
			Map.entry("Plus Size Collections", "plus-size"),
			Map.entry("Straight Pants", "straight-pants"),
			Map.entry("Tops", "tops"),
			Map.entry("Nighties", "nighties"),
			Map.entry("Offer Collections", "offer-collections"),
			Map.entry("Cord Sets", "co-ord-sets"),
			Map.entry("Frocks", "frocks"),
			Map.entry("Partywear Dresses", "partywear"));

	private static final int NEW_ARRIVAL_LIMIT = 4;

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<ProductResponse> list() {
		return productRepository.findAll().stream()
				.sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
				.map(this::toResponse)
				.toList();
	}

	public ProductResponse create(ProductRequest request) {
		Product product = new Product();
		String slug = uniqueSlug(blankToNull(request.slug()) == null ? slugify(request.name()) : slugify(request.slug()), "");
		product.setId(slug);
		apply(product, request, slug);
		return toResponse(productRepository.save(product));
	}

	public ProductResponse update(String id, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found."));
		String requested = blankToNull(request.slug()) == null ? product.getSlug() : slugify(request.slug());
		apply(product, request, uniqueSlug(requested, product.getId()));
		return toResponse(productRepository.save(product));
	}

	public void delete(String id) {
		if (!productRepository.existsById(id)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Product not found.");
		}
		productRepository.deleteById(id);
	}

	public void normalizeCatalogue() {
		for (Product product : productRepository.findAll()) {
			List<String> currentTags = new ArrayList<>(product.getTags());
			String category = remapCategory(product.getCategory(), currentTags);
			String categorySlug = CATEGORY_SLUG.get(category);
			if (categorySlug == null) {
				continue;
			}
			boolean isNew = currentTags.contains("new-arrival");
			boolean curated = currentTags.contains("curated");
			product.setCategory(category);
			product.setCategorySlug(categorySlug);
			product.getTags().clear();
			if (isNew) {
				product.getTags().add("new-arrival");
			}
			if (curated) {
				product.getTags().add("curated");
			}
			product.setBadge(isNew ? "New Arrival" : "");
			productRepository.save(product);
		}
		for (Product product : productRepository.findAll()) {
			boolean categoryHasCurated = productRepository.findAll().stream()
					.anyMatch(other -> product.getCategorySlug().equals(other.getCategorySlug()) && other.getTags().contains("curated"));
			if (!categoryHasCurated) {
				product.getTags().add("curated");
				productRepository.save(product);
			}
		}
	}

	private String remapCategory(String category, List<String> tags) {
		if (CATEGORY_SLUG.containsKey(category)) {
			return category;
		}
		if (tags.contains("palazzo")) {
			return "Palazzo Pants";
		}
		if (tags.contains("straight-pants")) {
			return "Straight Pants";
		}
		if (tags.contains("dress-material") || "Dress Materials".equals(category)) {
			return "Dress Materials";
		}
		if ("Plus Size".equals(category)) {
			return "Plus Size Collections";
		}
		if (tags.contains("tops")) {
			return "Tops";
		}
		if (tags.contains("nightwear")) {
			return "Nighties";
		}
		if (tags.contains("frock")) {
			return "Frocks";
		}
		if ("Partywear".equals(category) || tags.contains("partywear")) {
			return "Partywear Dresses";
		}
		if ("3-Piece Sets".equals(category) || tags.contains("3-piece")) {
			return "3 Piece Sets";
		}
		if ("Co-ord Sets".equals(category) || tags.contains("co-ord")) {
			return "Cord Sets";
		}
		if (tags.contains("offer")) {
			return "Offer Collections";
		}
		return category;
	}

	private void apply(Product product, ProductRequest request, String slug) {
		if (request.price() > request.originalPrice()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Discount price cannot be higher than the actual price.");
		}
		String categorySlug = CATEGORY_SLUG.get(request.category().trim());
		if (categorySlug == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a category from the list.");
		}
		List<String> tags = cleanTags(request.tags());
		if (tags.contains("new-arrival") && productRepository.countOtherNewArrivals(product.getId() == null ? "" : product.getId()) >= NEW_ARRIVAL_LIMIT) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "New arrivals can include only 4 products. Remove one before adding another.");
		}
		List<String> sizes = cleanSizes(request.sizes());
		if (sizes.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Select at least one size.");
		}
		product.setSlug(slug);
		product.setName(request.name().trim());
		product.setPrice(request.price());
		product.setOriginalPrice(request.originalPrice());
		product.setCategory(request.category().trim());
		product.setCategorySlug(categorySlug);
		product.getSizes().clear();
		product.getSizes().addAll(sizes);
		product.setImage(request.image().trim());
		product.setAltText(blankToEmpty(request.alt()));
		product.getTags().clear();
		product.getTags().addAll(tags);
		product.setStock(request.stock() == null || request.stock());
		product.setBadge(tags.contains("new-arrival") ? "New Arrival" : "");
		String description = blankToNull(request.description());
		product.setDescription(description == null ? describe(product) : description);
	}

	private List<String> cleanTags(List<String> tags) {
		if (tags == null) {
			return List.of();
		}
		LinkedHashSet<String> cleaned = new LinkedHashSet<>();
		for (String tag : tags) {
			if (tag == null) {
				continue;
			}
			String value = tag.trim().toLowerCase(Locale.ROOT);
			if (!TAGS.contains(value)) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown product tag: " + value);
			}
			cleaned.add(value);
		}
		return new ArrayList<>(cleaned);
	}

	private List<String> cleanSizes(List<String> sizes) {
		if (sizes == null) {
			return List.of();
		}
		LinkedHashSet<String> cleaned = new LinkedHashSet<>();
		for (String size : sizes) {
			if (size != null && !size.isBlank()) {
				cleaned.add(size.trim());
			}
		}
		return new ArrayList<>(cleaned);
	}

	private String uniqueSlug(String slug, String currentId) {
		String candidate = slug.isBlank() ? "product" : slug;
		String next = candidate;
		int suffix = 2;
		while (productRepository.existsBySlugAndIdNot(next, currentId == null ? "" : currentId)) {
			next = candidate + "-" + suffix;
			suffix += 1;
		}
		return next;
	}

	private String slugify(String value) {
		return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
	}

	private String describe(Product product) {
		String sizes = String.join(", ", product.getSizes());
		return product.getName() + " is part of the " + product.getCategory() + " collection at Shaani Clothing, Kodad."
				+ (sizes.isBlank() ? "" : " This style is available in " + sizes + ".");
	}

	private ProductResponse toResponse(Product product) {
		String image = product.getImage();
		return new ProductResponse(
				product.getId(),
				product.getSlug(),
				product.getName(),
				product.getPrice(),
				product.getOriginalPrice(),
				product.getCategory(),
				product.getCategorySlug(),
				product.getOccasion(),
				product.getFabric(),
				product.getSizes(),
				product.getBadge(),
				image,
				List.of(image),
				product.getAltText(),
				product.getTags(),
				product.isStock(),
				product.getDescription());
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private String blankToEmpty(String value) {
		return value == null ? "" : value.trim();
	}
}

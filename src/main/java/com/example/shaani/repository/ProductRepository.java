package com.example.shaani.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shaani.entity.Product;

public interface ProductRepository extends JpaRepository<Product, String> {

	boolean existsBySlugAndIdNot(String slug, String id);

	@Query("select count(distinct p.id) from Product p join p.tags t where t = 'new-arrival' and p.id <> :id")
	long countOtherNewArrivals(@Param("id") String id);
}

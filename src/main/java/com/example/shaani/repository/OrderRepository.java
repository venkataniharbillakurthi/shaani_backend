package com.example.shaani.repository;

import java.time.Instant;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shaani.entity.OrderStatus;
import com.example.shaani.entity.ShopOrder;

public interface OrderRepository extends JpaRepository<ShopOrder, Long> {

	@Query("""
			select coalesce(sum(o.subtotal), 0) from ShopOrder o
			where o.status in :statuses
			and o.processedAt >= :start
			and o.processedAt < :end
			""")
	long sumRevenue(
			@Param("statuses") Collection<OrderStatus> statuses,
			@Param("start") Instant start,
			@Param("end") Instant end);

	@Query("""
			select count(o) from ShopOrder o
			where o.status in :statuses
			and o.processedAt >= :start
			and o.processedAt < :end
			""")
	long countRevenueOrders(
			@Param("statuses") Collection<OrderStatus> statuses,
			@Param("start") Instant start,
			@Param("end") Instant end);
}

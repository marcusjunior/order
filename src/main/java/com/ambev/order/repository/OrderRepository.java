package com.ambev.order.repository;

import com.ambev.order.domain.entity.Order;
import com.ambev.order.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(UUID id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.externalId = :externalId")
    Optional<Order> findByExternalIdWithItems(String externalId);
}


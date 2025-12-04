package com.ambev.order.adapter.out.persistence;

import com.ambev.order.adapter.out.persistence.entity.OrderEntity;
import com.ambev.order.adapter.out.persistence.entity.OrderStatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Order Entity
 * This is the infrastructure layer - deals with database
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    Page<OrderEntity> findByStatus(OrderStatusEntity status, Pageable pageable);

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(UUID id);

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.externalId = :externalId")
    Optional<OrderEntity> findByExternalIdWithItems(String externalId);
}


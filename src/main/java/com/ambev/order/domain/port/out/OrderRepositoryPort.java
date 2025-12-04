package com.ambev.order.domain.port.out;

import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {
    OrderDomain save(OrderDomain order);
    Optional<OrderDomain> findById(UUID id);
    Optional<OrderDomain> findByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
    Page<OrderDomain> findAll(Pageable pageable);
    Page<OrderDomain> findByStatus(OrderStatusDomain status, Pageable pageable);
}


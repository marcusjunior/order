package com.ambev.order.domain.port.in;

import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface QueryOrderUseCase {
    OrderDomain findById(UUID id);
    OrderDomain findByExternalId(String externalId);
    Page<OrderDomain> findAll(Pageable pageable);
    Page<OrderDomain> findByStatus(OrderStatusDomain status, Pageable pageable);
}


package com.ambev.order.domain.service;

import com.ambev.order.application.exception.OrderNotFoundException;
import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import com.ambev.order.domain.port.in.QueryOrderUseCase;
import com.ambev.order.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryOrderService implements QueryOrderUseCase {

    private final OrderRepositoryPort repositoryPort;

    @Override
    @Transactional(readOnly = true)
    public OrderDomain findById(UUID id) {
        log.info("Domain: Fetching order by ID: {}", id);
        return repositoryPort.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDomain findByExternalId(String externalId) {
        log.info("Domain: Fetching order by external ID: {}", externalId);
        return repositoryPort.findByExternalId(externalId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with external ID: " + externalId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDomain> findAll(Pageable pageable) {
        log.info("Domain: Fetching all orders with pagination: {}", pageable);
        return repositoryPort.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDomain> findByStatus(OrderStatusDomain status, Pageable pageable) {
        log.info("Domain: Fetching orders by status: {} with pagination: {}", status, pageable);
        return repositoryPort.findByStatus(status, pageable);
    }
}


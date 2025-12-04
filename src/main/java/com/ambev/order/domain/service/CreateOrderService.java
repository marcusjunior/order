package com.ambev.order.domain.service;

import com.ambev.order.application.exception.DuplicateOrderException;
import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import com.ambev.order.domain.port.in.CreateOrderUseCase;
import com.ambev.order.domain.port.out.OrderCachePort;
import com.ambev.order.domain.port.out.OrderPublisherPort;
import com.ambev.order.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepositoryPort repositoryPort;
    private final OrderCachePort cachePort;
    private final OrderPublisherPort publisherPort;

    private static final String DUPLICATE_CHECK_PREFIX = "order:duplicate:";
    private static final long DUPLICATE_CHECK_TTL = 24;

    @Override
    @Transactional
    public OrderDomain createOrder(OrderDomain order) {
        log.info("Domain: Processing order with external ID: {}", order.getExternalId());

        if (isDuplicate(order.getExternalId())) {
            log.warn("Domain: Duplicate order detected: {}", order.getExternalId());
            throw new DuplicateOrderException("Order with external ID " + order.getExternalId() + " already exists");
        }

        if (!order.canBeProcessed()) {
            throw new IllegalArgumentException("Order cannot be processed: invalid items");
        }

        order.initializeNewOrder();
        order.getItems().forEach(item -> item.calculateTotalPrice());
        order.calculateTotalAmount();
        order.changeStatus(OrderStatusDomain.PROCESSING);

        OrderDomain savedOrder = repositoryPort.save(order);
        markAsProcessed(order.getExternalId());

        savedOrder.changeStatus(OrderStatusDomain.COMPLETED);
        savedOrder = repositoryPort.save(savedOrder);

        log.info("Domain: Order created successfully: {} with total amount: {}",
                savedOrder.getId(), savedOrder.getTotalAmount());

        publisherPort.publishOrder(savedOrder);

        return savedOrder;
    }

    private boolean isDuplicate(String externalId) {
        String key = DUPLICATE_CHECK_PREFIX + externalId;

        if (cachePort.exists(key)) {
            return true;
        }

        return repositoryPort.existsByExternalId(externalId);
    }

    private void markAsProcessed(String externalId) {
        String key = DUPLICATE_CHECK_PREFIX + externalId;
        cachePort.store(key, "processed", DUPLICATE_CHECK_TTL);
    }
}


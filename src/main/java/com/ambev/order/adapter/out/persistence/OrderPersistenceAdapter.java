package com.ambev.order.adapter.out.persistence;

import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import com.ambev.order.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter - Implementation of OrderRepositoryPort
 * Translates between domain model and JPA entities
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public OrderDomain save(OrderDomain order) {
        log.debug("Adapter: Saving order to database");
        var entity = mapper.toEntity(order);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OrderDomain> findById(UUID id) {
        log.debug("Adapter: Finding order by ID: {}", id);
        return jpaRepository.findByIdWithItems(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<OrderDomain> findByExternalId(String externalId) {
        log.debug("Adapter: Finding order by external ID: {}", externalId);
        return jpaRepository.findByExternalIdWithItems(externalId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByExternalId(String externalId) {
        log.debug("Adapter: Checking if order exists by external ID: {}", externalId);
        return jpaRepository.existsByExternalId(externalId);
    }

    @Override
    public Page<OrderDomain> findAll(Pageable pageable) {
        log.debug("Adapter: Finding all orders with pagination");
        return jpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<OrderDomain> findByStatus(OrderStatusDomain status, Pageable pageable) {
        log.debug("Adapter: Finding orders by status: {}", status);
        var entityStatus = mapper.toEntityStatus(status);
        return jpaRepository.findByStatus(entityStatus, pageable)
                .map(mapper::toDomain);
    }
}


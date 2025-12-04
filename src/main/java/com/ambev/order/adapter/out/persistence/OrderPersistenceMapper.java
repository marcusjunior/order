package com.ambev.order.adapter.out.persistence;

import com.ambev.order.adapter.out.persistence.entity.OrderEntity;
import com.ambev.order.adapter.out.persistence.entity.OrderItemEntity;
import com.ambev.order.adapter.out.persistence.entity.OrderStatusEntity;
import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderItemDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper between Domain models and JPA Entities
 */
@Component
public class OrderPersistenceMapper {

    public OrderEntity toEntity(OrderDomain domain) {
        if (domain == null) {
            return null;
        }

        OrderEntity entity = OrderEntity.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .status(toEntityStatus(domain.getStatus()))
                .totalAmount(domain.getTotalAmount())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .version(domain.getVersion())
                .build();

        if (domain.getItems() != null) {
            var items = domain.getItems().stream()
                    .map(itemDomain -> toItemEntity(itemDomain, entity))
                    .collect(Collectors.toList());
            entity.setItems(items);
        }

        return entity;
    }

    public OrderDomain toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        OrderDomain domain = OrderDomain.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .status(toDomainStatus(entity.getStatus()))
                .totalAmount(entity.getTotalAmount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion())
                .build();

        if (entity.getItems() != null) {
            var items = entity.getItems().stream()
                    .map(this::toItemDomain)
                    .collect(Collectors.toList());
            domain.setItems(items);
        }

        return domain;
    }

    public OrderItemEntity toItemEntity(OrderItemDomain domain, OrderEntity order) {
        if (domain == null) {
            return null;
        }

        return OrderItemEntity.builder()
                .id(domain.getId())
                .order(order)
                .productCode(domain.getProductCode())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .totalPrice(domain.getTotalPrice())
                .build();
    }

    public OrderItemDomain toItemDomain(OrderItemEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrderItemDomain.builder()
                .id(entity.getId())
                .productCode(entity.getProductCode())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalPrice(entity.getTotalPrice())
                .build();
    }

    public OrderStatusEntity toEntityStatus(OrderStatusDomain domain) {
        if (domain == null) {
            return null;
        }
        return OrderStatusEntity.valueOf(domain.name());
    }

    public OrderStatusDomain toDomainStatus(OrderStatusEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrderStatusDomain.valueOf(entity.name());
    }
}


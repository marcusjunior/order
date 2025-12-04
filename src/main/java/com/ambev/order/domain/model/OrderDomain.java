package com.ambev.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain model for Order - Pure domain entity without infrastructure concerns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDomain {

    private UUID id;
    private String externalId;
    private OrderStatusDomain status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    @Builder.Default
    private List<OrderItemDomain> items = new ArrayList<>();

    /**
     * Business rule: Calculate total amount from items
     */
    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(OrderItemDomain::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Business rule: Add item to order
     */
    public void addItem(OrderItemDomain item) {
        items.add(item);
    }

    /**
     * Business rule: Change order status
     */
    public void changeStatus(OrderStatusDomain newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Validate if order can be processed
     */
    public boolean canBeProcessed() {
        return items != null && !items.isEmpty()
                && items.stream().allMatch(OrderItemDomain::isValid);
    }

    /**
     * Business rule: Initialize new order
     */
    public void initializeNewOrder() {
        this.status = OrderStatusDomain.RECEIVED;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}


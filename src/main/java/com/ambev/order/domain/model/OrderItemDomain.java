package com.ambev.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain model for Order Item - Pure domain entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDomain {

    private UUID id;
    private String productCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    /**
     * Business rule: Calculate total price
     */
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    /**
     * Business rule: Validate item
     */
    public boolean isValid() {
        return productCode != null && !productCode.isEmpty()
                && quantity != null && quantity > 0
                && unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0;
    }
}


package com.ambev.order.dto;

import com.ambev.order.domain.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order response sent to External System B")
public class OrderResponseDTO {

    @Schema(description = "Internal order UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "External order ID from System A", example = "ORDER-12345")
    private String externalId;

    @Schema(description = "Current order status", example = "COMPLETED")
    private OrderStatus status;

    @Schema(description = "Total calculated amount", example = "150.00")
    private BigDecimal totalAmount;

    @Schema(description = "Order creation timestamp", example = "2025-11-06T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-11-06T10:30:05")
    private LocalDateTime updatedAt;

    @Schema(description = "List of order items with calculated totals")
    private List<OrderItemResponseDTO> items;
}

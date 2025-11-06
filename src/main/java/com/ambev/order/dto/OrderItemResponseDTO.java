package com.ambev.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item response with calculated total")
public class OrderItemResponseDTO {

    @Schema(description = "Internal item UUID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID id;

    @Schema(description = "Product code/SKU", example = "PROD-001")
    private String productCode;

    @Schema(description = "Quantity of the product", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price of the product", example = "50.00")
    private BigDecimal unitPrice;

    @Schema(description = "Calculated total price (quantity * unitPrice)", example = "100.00")
    private BigDecimal totalPrice;
}

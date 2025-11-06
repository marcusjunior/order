package com.ambev.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item details")
public class OrderItemRequestDTO {

    @NotBlank(message = "Product code is required")
    @Schema(description = "Product code/SKU", example = "PROD-001", required = true)
    private String productCode;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of the product", example = "2", required = true, minimum = "1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Schema(description = "Unit price of the product", example = "50.00", required = true)
    private BigDecimal unitPrice;
}

package com.ambev.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Order request from External System A")
public class OrderRequestDTO {

    @NotBlank(message = "External ID is required")
    @Schema(description = "External order ID from System A", example = "ORDER-12345", required = true)
    private String externalId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    @Schema(description = "List of order items", required = true)
    private List<OrderItemRequestDTO> items;
}

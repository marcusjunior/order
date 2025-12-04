package com.ambev.order.adapter.in.rest;

import com.ambev.order.domain.model.OrderStatusDomain;
import com.ambev.order.domain.port.in.CreateOrderUseCase;
import com.ambev.order.domain.port.in.QueryOrderUseCase;
import com.ambev.order.application.dto.OrderRequestDTO;
import com.ambev.order.application.dto.OrderResponseDTO;
import com.ambev.order.application.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Adapter - Receives HTTP requests and delegates to domain use cases
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management endpoints for external systems integration")
public class OrderRestAdapter {

    private final CreateOrderUseCase createOrderUseCase;
    private final QueryOrderUseCase queryOrderUseCase;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(summary = "Create a new order",
            description = "Receives order from External System A, calculates total amount, and processes it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Duplicate order detected")
    })
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO requestDTO) {
        log.info("REST Adapter: Creating order with external ID: {}", requestDTO.getExternalId());

        var orderDomain = orderMapper.toDomain(requestDTO);
        var createdOrder = createOrderUseCase.createOrder(orderDomain);
        var response = orderMapper.toResponseDTO(createdOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID",
            description = "Retrieves a specific order by its internal UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "Order UUID") @PathVariable UUID id) {
        log.info("REST Adapter: Fetching order by ID: {}", id);

        var orderDomain = queryOrderUseCase.findById(id);
        var response = orderMapper.toResponseDTO(orderDomain);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/{externalId}")
    @Operation(summary = "Get order by external ID",
            description = "Retrieves a specific order by external system ID (from System A)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponseDTO> getOrderByExternalId(
            @Parameter(description = "External order ID from System A") @PathVariable String externalId) {
        log.info("REST Adapter: Fetching order by external ID: {}", externalId);

        var orderDomain = queryOrderUseCase.findByExternalId(externalId);
        var response = orderMapper.toResponseDTO(orderDomain);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all orders",
            description = "Retrieves paginated list of all orders for External System B. " +
                    "Use query parameters: page (default 0), size (default 20), sort (e.g., createdAt,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST Adapter: Fetching all orders");

        Page<OrderResponseDTO> response = queryOrderUseCase.findAll(pageable)
                .map(orderMapper::toResponseDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status",
            description = "Retrieves paginated list of orders filtered by status. " +
                    "Use query parameters: page (default 0), size (default 20), sort (e.g., createdAt,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByStatus(
            @Parameter(description = "Order status (RECEIVED, PROCESSING, COMPLETED, FAILED)")
            @PathVariable OrderStatusDomain status,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST Adapter: Fetching orders by status: {}", status);

        Page<OrderResponseDTO> response = queryOrderUseCase.findByStatus(status, pageable)
                .map(orderMapper::toResponseDTO);

        return ResponseEntity.ok(response);
    }
}


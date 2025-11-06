package com.ambev.order.controller;

import com.ambev.order.domain.entity.OrderStatus;
import com.ambev.order.dto.OrderItemRequestDTO;
import com.ambev.order.dto.OrderRequestDTO;
import com.ambev.order.dto.OrderResponseDTO;
import com.ambev.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderRequestDTO orderRequestDTO;
    private OrderResponseDTO orderResponseDTO;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        orderRequestDTO = OrderRequestDTO.builder()
                .externalId("ORDER-001")
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .productCode("PROD-001")
                                .quantity(2)
                                .unitPrice(new BigDecimal("50.00"))
                                .build()
                ))
                .build();

        orderResponseDTO = OrderResponseDTO.builder()
                .id(orderId)
                .externalId("ORDER-001")
                .status(OrderStatus.COMPLETED)
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(orderResponseDTO);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("ORDER-001"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    @Test
    void shouldReturnBadRequestWhenExternalIdIsNull() throws Exception {
        orderRequestDTO.setExternalId(null);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenItemsAreEmpty() throws Exception {
        orderRequestDTO.setItems(new ArrayList<>());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetOrderByIdSuccessfully() throws Exception {
        when(orderService.getOrderById(orderId)).thenReturn(orderResponseDTO);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.externalId").value("ORDER-001"));
    }

    @Test
    void shouldGetOrderByExternalIdSuccessfully() throws Exception {
        when(orderService.getOrderByExternalId("ORDER-001")).thenReturn(orderResponseDTO);

        mockMvc.perform(get("/api/v1/orders/external/{externalId}", "ORDER-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("ORDER-001"));
    }

    @Test
    void shouldGetAllOrdersSuccessfully() throws Exception {
        Page<OrderResponseDTO> page = new PageImpl<>(
                List.of(orderResponseDTO),
                PageRequest.of(0, 20),
                1
        );

        when(orderService.getAllOrders(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].externalId").value("ORDER-001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldGetOrdersByStatusSuccessfully() throws Exception {
        Page<OrderResponseDTO> page = new PageImpl<>(
                List.of(orderResponseDTO),
                PageRequest.of(0, 20),
                1
        );

        when(orderService.getOrdersByStatus(eq(OrderStatus.COMPLETED), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders/status/{status}", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}


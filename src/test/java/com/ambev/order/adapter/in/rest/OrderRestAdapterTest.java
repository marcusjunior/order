package com.ambev.order.adapter.in.rest;

import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderItemDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import com.ambev.order.domain.port.in.CreateOrderUseCase;
import com.ambev.order.domain.port.in.QueryOrderUseCase;
import com.ambev.order.application.dto.OrderItemRequestDTO;
import com.ambev.order.application.dto.OrderRequestDTO;
import com.ambev.order.application.dto.OrderResponseDTO;
import com.ambev.order.application.mapper.OrderMapper;
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

@WebMvcTest(OrderRestAdapter.class)
class OrderRestAdapterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @MockBean
    private QueryOrderUseCase queryOrderUseCase;

    @MockBean
    private OrderMapper orderMapper;

    private OrderRequestDTO orderRequestDTO;
    private OrderResponseDTO orderResponseDTO;
    private OrderDomain orderDomain;
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

        OrderItemDomain itemDomain = OrderItemDomain.builder()
                .productCode("PROD-001")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .totalPrice(BigDecimal.valueOf(100.00))
                .build();

        orderDomain = OrderDomain.builder()
                .id(orderId)
                .externalId("ORDER-001")
                .status(OrderStatusDomain.COMPLETED)
                .totalAmount(BigDecimal.valueOf(100.00))
                .items(List.of(itemDomain))
                .build();

        orderResponseDTO = OrderResponseDTO.builder()
                .id(orderId)
                .externalId("ORDER-001")
                .status(OrderStatusDomain.COMPLETED)
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        when(orderMapper.toDomain(any(OrderRequestDTO.class))).thenReturn(orderDomain);
        when(createOrderUseCase.createOrder(any(OrderDomain.class))).thenReturn(orderDomain);
        when(orderMapper.toResponseDTO(any(OrderDomain.class))).thenReturn(orderResponseDTO);

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
        when(queryOrderUseCase.findById(orderId)).thenReturn(orderDomain);
        when(orderMapper.toResponseDTO(any(OrderDomain.class))).thenReturn(orderResponseDTO);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.externalId").value("ORDER-001"));
    }

    @Test
    void shouldGetOrderByExternalIdSuccessfully() throws Exception {
        when(queryOrderUseCase.findByExternalId("ORDER-001")).thenReturn(orderDomain);
        when(orderMapper.toResponseDTO(any(OrderDomain.class))).thenReturn(orderResponseDTO);

        mockMvc.perform(get("/api/v1/orders/external/{externalId}", "ORDER-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("ORDER-001"));
    }

    @Test
    void shouldGetAllOrdersSuccessfully() throws Exception {
        Page<OrderDomain> domainPage = new PageImpl<>(
                List.of(orderDomain),
                PageRequest.of(0, 20),
                1
        );

        Page<OrderResponseDTO> page = new PageImpl<>(
                List.of(orderResponseDTO),
                PageRequest.of(0, 20),
                1
        );

        when(queryOrderUseCase.findAll(any())).thenReturn(domainPage);
        when(orderMapper.toResponseDTO(any(OrderDomain.class))).thenReturn(orderResponseDTO);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].externalId").value("ORDER-001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldGetOrdersByStatusSuccessfully() throws Exception {
        Page<OrderDomain> domainPage = new PageImpl<>(
                List.of(orderDomain),
                PageRequest.of(0, 20),
                1
        );

        when(queryOrderUseCase.findByStatus(eq(OrderStatusDomain.COMPLETED), any())).thenReturn(domainPage);
        when(orderMapper.toResponseDTO(any(OrderDomain.class))).thenReturn(orderResponseDTO);

        mockMvc.perform(get("/api/v1/orders/status/{status}", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}


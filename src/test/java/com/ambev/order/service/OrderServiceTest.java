package com.ambev.order.service;

import com.ambev.order.domain.entity.Order;
import com.ambev.order.domain.entity.OrderItem;
import com.ambev.order.domain.entity.OrderStatus;
import com.ambev.order.dto.OrderItemRequestDTO;
import com.ambev.order.dto.OrderRequestDTO;
import com.ambev.order.dto.OrderResponseDTO;
import com.ambev.order.exception.DuplicateOrderException;
import com.ambev.order.exception.OrderNotFoundException;
import com.ambev.order.mapper.OrderMapper;
import com.ambev.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private OrderPublisherService publisherService;

    @InjectMocks
    private OrderService orderService;

    private OrderRequestDTO orderRequestDTO;
    private Order order;
    private OrderResponseDTO orderResponseDTO;

    @BeforeEach
    void setUp() {
        orderRequestDTO = OrderRequestDTO.builder()
                .externalId("ORDER-001")
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .productCode("PROD-001")
                                .quantity(2)
                                .unitPrice(new BigDecimal("50.00"))
                                .build(),
                        OrderItemRequestDTO.builder()
                                .productCode("PROD-002")
                                .quantity(1)
                                .unitPrice(new BigDecimal("30.00"))
                                .build()
                ))
                .build();

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setExternalId("ORDER-001");
        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalAmount(new BigDecimal("130.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        OrderItem item1 = new OrderItem();
        item1.setId(UUID.randomUUID());
        item1.setProductCode("PROD-001");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("50.00"));
        item1.setTotalPrice(new BigDecimal("100.00"));
        item1.setOrder(order);

        OrderItem item2 = new OrderItem();
        item2.setId(UUID.randomUUID());
        item2.setProductCode("PROD-002");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("30.00"));
        item2.setTotalPrice(new BigDecimal("30.00"));
        item2.setOrder(order);

        order.getItems().add(item1);
        order.getItems().add(item2);

        orderResponseDTO = OrderResponseDTO.builder()
                .id(order.getId())
                .externalId("ORDER-001")
                .status(OrderStatus.COMPLETED)
                .totalAmount(new BigDecimal("130.00"))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(orderRepository.existsByExternalId(anyString())).thenReturn(false);
        when(orderMapper.toEntity(any(OrderRequestDTO.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        assertNotNull(result);
        assertEquals("ORDER-001", result.getExternalId());
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        assertEquals(new BigDecimal("130.00"), result.getTotalAmount());

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(publisherService).publishOrderToExternalSystem(any(Order.class));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void shouldThrowDuplicateOrderExceptionWhenOrderExistsInRedis() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        assertThrows(DuplicateOrderException.class, () -> orderService.createOrder(orderRequestDTO));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowDuplicateOrderExceptionWhenOrderExistsInDatabase() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(orderRepository.existsByExternalId(anyString())).thenReturn(true);

        assertThrows(DuplicateOrderException.class, () -> orderService.createOrder(orderRequestDTO));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldGetOrderByIdSuccessfully() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals("ORDER-001", result.getExternalId());
        verify(orderRepository).findByIdWithItems(orderId);
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));

        verify(orderRepository).findByIdWithItems(orderId);
    }

    @Test
    void shouldGetOrderByExternalIdSuccessfully() {
        when(orderRepository.findByExternalIdWithItems("ORDER-001")).thenReturn(Optional.of(order));
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.getOrderByExternalId("ORDER-001");

        assertNotNull(result);
        assertEquals("ORDER-001", result.getExternalId());
        verify(orderRepository).findByExternalIdWithItems("ORDER-001");
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenExternalIdDoesNotExist() {
        when(orderRepository.findByExternalIdWithItems("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderByExternalId("NON-EXISTENT"));

        verify(orderRepository).findByExternalIdWithItems("NON-EXISTENT");
    }

    @Test
    void shouldGetAllOrdersWithPagination() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Order> orders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(orderResponseDTO);

        Page<OrderResponseDTO> result = orderService.getAllOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void shouldGetOrdersByStatus() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Order> orders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findByStatus(OrderStatus.COMPLETED, pageable)).thenReturn(orderPage);
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(orderResponseDTO);

        Page<OrderResponseDTO> result = orderService.getOrdersByStatus(OrderStatus.COMPLETED, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(OrderStatus.COMPLETED, result.getContent().get(0).getStatus());
        verify(orderRepository).findByStatus(OrderStatus.COMPLETED, pageable);
    }
}

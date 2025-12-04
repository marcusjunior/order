package com.ambev.order.domain.service;

import com.ambev.order.application.exception.OrderNotFoundException;
import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderItemDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import com.ambev.order.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryOrderServiceTest {

    @Mock
    private OrderRepositoryPort repositoryPort;

    private QueryOrderService service;
    private OrderDomain orderDomain;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        service = new QueryOrderService(repositoryPort);
        orderId = UUID.randomUUID();
        setupOrderDomain();
    }

    private void setupOrderDomain() {
        OrderItemDomain item = OrderItemDomain.builder()
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
                .items(List.of(item))
                .build();
    }

    @Test
    void shouldFindOrderByIdSuccessfully() {
        when(repositoryPort.findById(orderId)).thenReturn(Optional.of(orderDomain));

        OrderDomain result = service.findById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("ORDER-001", result.getExternalId());
        verify(repositoryPort).findById(orderId);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundById() {
        when(repositoryPort.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.findById(orderId));

        verify(repositoryPort).findById(orderId);
    }

    @Test
    void shouldFindOrderByExternalIdSuccessfully() {
        when(repositoryPort.findByExternalId("ORDER-001")).thenReturn(Optional.of(orderDomain));

        OrderDomain result = service.findByExternalId("ORDER-001");

        assertNotNull(result);
        assertEquals("ORDER-001", result.getExternalId());
        verify(repositoryPort).findByExternalId("ORDER-001");
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundByExternalId() {
        when(repositoryPort.findByExternalId("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.findByExternalId("NON-EXISTENT"));

        verify(repositoryPort).findByExternalId("NON-EXISTENT");
    }

    @Test
    void shouldFindAllOrdersSuccessfully() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<OrderDomain> page = new PageImpl<>(List.of(orderDomain), pageable, 1);

        when(repositoryPort.findAll(pageable)).thenReturn(page);

        Page<OrderDomain> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(repositoryPort).findAll(pageable);
    }

    @Test
    void shouldFindOrdersByStatusSuccessfully() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<OrderDomain> page = new PageImpl<>(List.of(orderDomain), pageable, 1);

        when(repositoryPort.findByStatus(OrderStatusDomain.COMPLETED, pageable)).thenReturn(page);

        Page<OrderDomain> result = service.findByStatus(OrderStatusDomain.COMPLETED, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(OrderStatusDomain.COMPLETED, result.getContent().get(0).getStatus());
        verify(repositoryPort).findByStatus(OrderStatusDomain.COMPLETED, pageable);
    }
}


package com.ambev.order.domain.service;

import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderItemDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import com.ambev.order.domain.port.out.OrderCachePort;
import com.ambev.order.domain.port.out.OrderPublisherPort;
import com.ambev.order.domain.port.out.OrderRepositoryPort;
import com.ambev.order.application.exception.DuplicateOrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderRepositoryPort repositoryPort;

    @Mock
    private OrderCachePort cachePort;

    @Mock
    private OrderPublisherPort publisherPort;

    private CreateOrderService service;
    private OrderDomain orderDomain;

    @BeforeEach
    void setUp() {
        service = new CreateOrderService(repositoryPort, cachePort, publisherPort);
        setupOrderDomain();
    }

    private void setupOrderDomain() {
        OrderItemDomain item1 = OrderItemDomain.builder()
                .productCode("PROD-001")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .totalPrice(BigDecimal.valueOf(100.00))
                .build();

        OrderItemDomain item2 = OrderItemDomain.builder()
                .productCode("PROD-002")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(30.00))
                .totalPrice(BigDecimal.valueOf(30.00))
                .build();

        orderDomain = OrderDomain.builder()
                .externalId("ORDER-001")
                .items(List.of(item1, item2))
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        when(cachePort.exists(anyString())).thenReturn(false);
        when(repositoryPort.existsByExternalId(anyString())).thenReturn(false);
        when(repositoryPort.save(any(OrderDomain.class))).thenAnswer(invocation -> {
            OrderDomain saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        OrderDomain result = service.createOrder(orderDomain);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("ORDER-001", result.getExternalId());
        assertEquals(OrderStatusDomain.COMPLETED, result.getStatus());
        assertEquals(BigDecimal.valueOf(130.00), result.getTotalAmount());

        verify(repositoryPort, times(2)).save(any(OrderDomain.class));
        verify(publisherPort).publishOrder(any(OrderDomain.class));
        verify(cachePort).store(anyString(), anyString(), anyLong());
    }

    @Test
    void shouldThrowDuplicateOrderExceptionWhenOrderExistsInCache() {
        when(cachePort.exists(anyString())).thenReturn(true);

        assertThrows(DuplicateOrderException.class, () -> service.createOrder(orderDomain));

        verify(repositoryPort, never()).save(any(OrderDomain.class));
    }

    @Test
    void shouldThrowDuplicateOrderExceptionWhenOrderExistsInDatabase() {
        when(cachePort.exists(anyString())).thenReturn(false);
        when(repositoryPort.existsByExternalId(anyString())).thenReturn(true);

        assertThrows(DuplicateOrderException.class, () -> service.createOrder(orderDomain));

        verify(repositoryPort, never()).save(any(OrderDomain.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderHasNoItems() {
        orderDomain.setItems(List.of());

        when(cachePort.exists(anyString())).thenReturn(false);
        when(repositoryPort.existsByExternalId(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.createOrder(orderDomain));

        verify(repositoryPort, never()).save(any(OrderDomain.class));
    }
}

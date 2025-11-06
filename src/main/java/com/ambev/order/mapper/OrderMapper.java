package com.ambev.order.mapper;

import com.ambev.order.domain.entity.Order;
import com.ambev.order.domain.entity.OrderItem;
import com.ambev.order.domain.entity.OrderStatus;
import com.ambev.order.dto.OrderItemRequestDTO;
import com.ambev.order.dto.OrderItemResponseDTO;
import com.ambev.order.dto.OrderRequestDTO;
import com.ambev.order.dto.OrderResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toEntity(OrderRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Order order = Order.builder()
                .externalId(dto.getExternalId())
                .status(OrderStatus.RECEIVED)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        if (dto.getItems() != null) {
            List<OrderItem> items = dto.getItems().stream()
                    .map(itemDto -> toItemEntity(itemDto))
                    .collect(Collectors.toList());

            items.forEach(item -> item.setOrder(order));
            order.setItems(items);

            BigDecimal total = items.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);
        }

        return order;
    }

    public OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponseDTO.builder()
                .id(order.getId())
                .externalId(order.getExternalId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(toItemResponseDTOList(order.getItems()))
                .build();
    }

    public List<OrderResponseDTO> toResponseDTOList(List<Order> orders) {
        if (orders == null) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderItem toItemEntity(OrderItemRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        BigDecimal totalPrice = dto.getUnitPrice() != null && dto.getQuantity() != null
                ? dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity()))
                : BigDecimal.ZERO;

        return OrderItem.builder()
                .productCode(dto.getProductCode())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .totalPrice(totalPrice)
                .build();
    }

    public OrderItemResponseDTO toItemResponseDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productCode(item.getProductCode())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    public List<OrderItem> toItemEntityList(List<OrderItemRequestDTO> dtos) {
        if (dtos == null) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::toItemEntity)
                .collect(Collectors.toList());
    }

    public List<OrderItemResponseDTO> toItemResponseDTOList(List<OrderItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(this::toItemResponseDTO)
                .collect(Collectors.toList());
    }
}

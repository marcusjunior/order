package com.ambev.order.application.mapper;

import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.model.OrderItemDomain;
import com.ambev.order.domain.model.OrderStatusDomain;
import com.ambev.order.application.dto.OrderItemRequestDTO;
import com.ambev.order.application.dto.OrderItemResponseDTO;
import com.ambev.order.application.dto.OrderRequestDTO;
import com.ambev.order.application.dto.OrderResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDomain toDomain(OrderRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        OrderDomain domain = OrderDomain.builder()
                .externalId(dto.getExternalId())
                .status(OrderStatusDomain.RECEIVED)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        if (dto.getItems() != null) {
            List<OrderItemDomain> items = dto.getItems().stream()
                    .map(this::toItemDomain)
                    .collect(Collectors.toList());
            domain.setItems(items);
        }

        return domain;
    }

    public OrderResponseDTO toResponseDTO(OrderDomain domain) {
        if (domain == null) {
            return null;
        }

        return OrderResponseDTO.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .status(domain.getStatus())
                .totalAmount(domain.getTotalAmount())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .items(toItemResponseDTOList(domain.getItems()))
                .build();
    }

    public OrderItemDomain toItemDomain(OrderItemRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        BigDecimal totalPrice = dto.getUnitPrice() != null && dto.getQuantity() != null
                ? dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity()))
                : BigDecimal.ZERO;

        return OrderItemDomain.builder()
                .productCode(dto.getProductCode())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .totalPrice(totalPrice)
                .build();
    }

    public OrderItemResponseDTO toItemResponseDTO(OrderItemDomain item) {
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

    public List<OrderItemResponseDTO> toItemResponseDTOList(List<OrderItemDomain> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(this::toItemResponseDTO)
                .collect(Collectors.toList());
    }
}

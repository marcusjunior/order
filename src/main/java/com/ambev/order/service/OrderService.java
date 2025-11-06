package com.ambev.order.service;

import com.ambev.order.domain.entity.Order;
import com.ambev.order.domain.entity.OrderStatus;
import com.ambev.order.dto.OrderRequestDTO;
import com.ambev.order.dto.OrderResponseDTO;
import com.ambev.order.exception.DuplicateOrderException;
import com.ambev.order.exception.OrderNotFoundException;
import com.ambev.order.mapper.OrderMapper;
import com.ambev.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final OrderPublisherService publisherService;

    private static final String DUPLICATE_CHECK_PREFIX = "order:duplicate:";
    private static final long DUPLICATE_CHECK_TTL = 24;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        log.info("Processing order with external ID: {}", requestDTO.getExternalId());

        if (isDuplicate(requestDTO.getExternalId())) {
            log.warn("Duplicate order detected: {}", requestDTO.getExternalId());
            throw new DuplicateOrderException("Order with external ID " + requestDTO.getExternalId() + " already exists");
        }

        Order order = orderMapper.toEntity(requestDTO);
        order.setStatus(OrderStatus.PROCESSING);

        Order savedOrder = orderRepository.save(order);
        markAsProcessed(requestDTO.getExternalId());

        savedOrder.setStatus(OrderStatus.COMPLETED);
        savedOrder = orderRepository.save(savedOrder);

        log.info("Order created successfully: {} with total amount: {}",
                savedOrder.getId(), savedOrder.getTotalAmount());

        publisherService.publishOrderToExternalSystem(savedOrder);

        return orderMapper.toResponseDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(UUID id) {
        log.info("Fetching order by ID: {}", id);
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));
        return orderMapper.toResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByExternalId(String externalId) {
        log.info("Fetching order by external ID: {}", externalId);
        Order order = orderRepository.findByExternalIdWithItems(externalId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with external ID: " + externalId));
        return orderMapper.toResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        log.info("Fetching all orders with pagination: {}", pageable);
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.info("Fetching orders by status: {} with pagination: {}", status, pageable);
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        return orders.map(orderMapper::toResponseDTO);
    }

    private boolean isDuplicate(String externalId) {
        String key = DUPLICATE_CHECK_PREFIX + externalId;
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            return true;
        }

        return orderRepository.existsByExternalId(externalId);
    }

    private void markAsProcessed(String externalId) {
        String key = DUPLICATE_CHECK_PREFIX + externalId;
        redisTemplate.opsForValue().set(key, "processed", DUPLICATE_CHECK_TTL, TimeUnit.HOURS);
    }
}

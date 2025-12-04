package com.ambev.order.adapter.in.messaging;

import com.ambev.order.domain.port.in.CreateOrderUseCase;
import com.ambev.order.application.dto.OrderRequestDTO;
import com.ambev.order.application.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Messaging Adapter - Receives messages from RabbitMQ and delegates to domain use cases
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumerAdapter {

    private final CreateOrderUseCase createOrderUseCase;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;

    @RabbitListener(queues = "${order.queue.incoming}")
    public void consumeOrderFromExternalSystem(String message) {
        try {
            log.info("Messaging Adapter: Received order from external system A");

            OrderRequestDTO orderRequest = objectMapper.readValue(message, OrderRequestDTO.class);
            var orderDomain = orderMapper.toDomain(orderRequest);

            createOrderUseCase.createOrder(orderDomain);

            log.info("Messaging Adapter: Order processed successfully from queue");
        } catch (Exception e) {
            log.error("Messaging Adapter: Error processing order from queue", e);
            // Here you could implement DLQ (Dead Letter Queue) logic
        }
    }
}


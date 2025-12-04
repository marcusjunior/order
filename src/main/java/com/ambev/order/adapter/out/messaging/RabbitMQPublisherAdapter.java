package com.ambev.order.adapter.out.messaging;

import com.ambev.order.domain.model.OrderDomain;
import com.ambev.order.domain.port.out.OrderPublisherPort;
import com.ambev.order.application.dto.OrderResponseDTO;
import com.ambev.order.application.mapper.OrderMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter - RabbitMQ implementation of OrderPublisherPort
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQPublisherAdapter implements OrderPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;

    @Value("${order.exchange}")
    private String exchange;

    @Value("${order.queue.outgoing}")
    private String outgoingQueue;

    @Override
    public void publishOrder(OrderDomain order) {
        try {
            log.info("Adapter: Publishing order to external system: {}", order.getId());

            // Convert domain to DTO for external communication
            OrderResponseDTO responseDTO = orderMapper.toResponseDTO(order);
            String message = objectMapper.writeValueAsString(responseDTO);

            rabbitTemplate.convertAndSend(exchange, outgoingQueue, message);
            log.info("Adapter: Order published successfully: {}", order.getId());
        } catch (JsonProcessingException e) {
            log.error("Adapter: Error publishing order: {}", order.getId(), e);
            throw new RuntimeException("Failed to publish order", e);
        }
    }
}


package com.ambev.order.service;

import com.ambev.order.domain.entity.Order;
import com.ambev.order.dto.OrderResponseDTO;
import com.ambev.order.mapper.OrderMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    @Value("${order.exchange}")
    private String exchange;

    @Value("${order.queue.outgoing}")
    private String outgoingQueue;

    public void publishOrderToExternalSystem(Order order) {
        try {
            OrderResponseDTO responseDTO = orderMapper.toResponseDTO(order);
            String message = objectMapper.writeValueAsString(responseDTO);

            rabbitTemplate.convertAndSend(exchange, outgoingQueue, message);
            log.info("Order published to external system B: {}", order.getId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing order to external system: {}", order.getId(), e);
        }
    }
}

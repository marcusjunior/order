package com.ambev.order.service;

import com.ambev.order.dto.OrderRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumerService {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${order.queue.incoming}")
    public void consumeOrderFromExternalSystem(String message) {
        try {
            log.info("Received order from external system A");
            OrderRequestDTO orderRequest = objectMapper.readValue(message, OrderRequestDTO.class);
            orderService.createOrder(orderRequest);
            log.info("Order processed successfully from queue");
        } catch (Exception e) {
            log.error("Error processing order from queue", e);
        }
    }
}


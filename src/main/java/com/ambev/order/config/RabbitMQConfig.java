package com.ambev.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${order.queue.incoming}")
    private String incomingQueue;

    @Value("${order.queue.outgoing}")
    private String outgoingQueue;

    @Value("${order.exchange}")
    private String exchange;

    @Bean
    public Queue incomingQueue() {
        return QueueBuilder.durable(incomingQueue)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Queue outgoingQueue() {
        return QueueBuilder.durable(outgoingQueue)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding incomingBinding(Queue incomingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(incomingQueue).to(exchange).with(incomingQueue.getName());
    }

    @Bean
    public Binding outgoingBinding(Queue outgoingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(outgoingQueue).to(exchange).with(outgoingQueue.getName());
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(10);
        factory.setMaxConcurrentConsumers(20);
        factory.setPrefetchCount(50);
        return factory;
    }
}


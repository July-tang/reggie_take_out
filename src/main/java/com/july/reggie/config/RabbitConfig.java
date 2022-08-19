package com.july.reggie.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author july
 */
@Configuration
public class RabbitConfig {

    public static final String DELAYED_EXCHANGE_NAME = "delayed.exchange";

    public static final String DELAYED_QUEUE_NAME = "delayed.queue";

    public static final String DELAYED_ROUTING_KEY = "delayed.routingKey";

    public static final String ORDER_EXCHANGE_NAME = "order_exchange";

    public static final String ORDER_QUEUE_NAME = "order_queue";

    public static final String ORDER_ROUTING_KEY = "order_routingKey";

    @Bean
    public Queue delayedQueue() {
        return new Queue(DELAYED_QUEUE_NAME);
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE_NAME).build();
    }

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message",
                true, false, arguments);
    }

    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    public Binding delayedQueueBindDelayedExchange(Queue delayedQueue,
                                                   CustomExchange delayedExchange) {
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding orderQueueBindOrderExchange(Queue orderQueue,
                                               DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ORDER_ROUTING_KEY);
    }
}

package com.july.reggie.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author july
 */
@Configuration
public class RabbitConfig {

    private final String EXCHANGE_NAME = "order_topic_exchange";

    private final String QUEUE_NAME = "order_queue";

}

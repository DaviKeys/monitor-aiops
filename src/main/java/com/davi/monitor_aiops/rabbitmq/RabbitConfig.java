package com.davi.monitor_aiops.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Nome oficial da nossa fila
    public static final String QUEUE_NAME = "metrics.queue";

    @Bean
    public Queue metricsQueue() {
        // O "true" significa que a fila é durável (sobrevive se o Docker reiniciar)
        return new Queue(QUEUE_NAME, true);
    }
}
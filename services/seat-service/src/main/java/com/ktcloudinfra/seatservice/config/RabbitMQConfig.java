package com.ktcloudinfra.seatservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RESERVATION_EXCHANGE = "reservation.exchange";
    public static final String RESERVATION_REQUESTED_QUEUE = "reservation.requested.queue";
    public static final String RESERVATION_CANCELLED_QUEUE = "reservation.cancelled.queue";

    @Bean
    public DirectExchange reservationExchange() {
        return new DirectExchange(RESERVATION_EXCHANGE);
    }

    @Bean
    public Queue reservationRequestedQueue() {
        return new Queue(RESERVATION_REQUESTED_QUEUE, true);
    }

    @Bean
    public Queue reservationCancelledQueue() {
        return new Queue(RESERVATION_CANCELLED_QUEUE, true);
    }

    @Bean
    public Binding requestedBinding() {
        return BindingBuilder.bind(reservationRequestedQueue())
            .to(reservationExchange()).with("reservation.requested");
    }

    @Bean
    public Binding cancelledBinding() {
        return BindingBuilder.bind(reservationCancelledQueue())
            .to(reservationExchange()).with("reservation.cancelled");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

package com.bancobase.payments.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentStatusEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;

    public PaymentStatusEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.payment-status-exchange}") String exchangeName
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }

    public void publish(PaymentStatusChangedEvent event) {
        rabbitTemplate.convertAndSend(exchangeName, "", event);
    }
}

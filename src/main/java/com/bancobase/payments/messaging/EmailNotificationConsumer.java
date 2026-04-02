package com.bancobase.payments.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.payment-status-queue-notify}")
    public void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        log.info("[notify-email] Payment {} status {} -> {}", event.paymentId(), event.previousStatus(), event.newStatus());
    }
}

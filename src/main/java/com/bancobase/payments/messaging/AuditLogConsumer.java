package com.bancobase.payments.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuditLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditLogConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.payment-status-queue-audit}")
    public void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        log.info("[audit] paymentId={} concepto={} transition={}->{} at={}",
                event.paymentId(),
                event.concepto(),
                event.previousStatus(),
                event.newStatus(),
                event.occurredAt());
    }
}

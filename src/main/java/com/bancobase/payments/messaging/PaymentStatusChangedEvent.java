package com.bancobase.payments.messaging;

import com.bancobase.payments.payment.model.PaymentStatus;

import java.time.Instant;

public record PaymentStatusChangedEvent(
        String paymentId,
        PaymentStatus previousStatus,
        PaymentStatus newStatus,
        Instant occurredAt,
        String concepto
) {
}

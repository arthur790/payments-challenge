package com.bancobase.payments.payment.dto;

import com.bancobase.payments.payment.model.Payment;
import com.bancobase.payments.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String concepto,
        int cantidadProductos,
        String pagador,
        String beneficiario,
        BigDecimal montoTotal,
        PaymentStatus estatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getConcepto(),
                p.getCantidadProductos(),
                p.getPagador(),
                p.getBeneficiario(),
                p.getMontoTotal(),
                p.getEstatus(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

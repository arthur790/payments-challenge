package com.bancobase.payments.payment.dto;

import com.bancobase.payments.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(@NotNull PaymentStatus estatus) {
}

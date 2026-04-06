package com.bancobase.payments.payment.dto;

import com.bancobase.payments.payment.model.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank String concepto,
        @Positive int cantidadProductos,
        @NotBlank String pagador,
        @NotBlank String beneficiario,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal montoTotal,
        @NotNull PaymentStatus estatus
) {
}

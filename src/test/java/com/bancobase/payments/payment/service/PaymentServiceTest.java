package com.bancobase.payments.payment.service;

import com.bancobase.payments.messaging.PaymentStatusChangedEvent;
import com.bancobase.payments.messaging.PaymentStatusEventPublisher;
import com.bancobase.payments.payment.Payment;
import com.bancobase.payments.payment.PaymentNotFoundException;
import com.bancobase.payments.payment.PaymentStatus;
import com.bancobase.payments.payment.dto.CreatePaymentRequest;
import com.bancobase.payments.payment.dto.UpdatePaymentStatusRequest;
import com.bancobase.payments.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStatusEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void create_persistsPayment() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId("507f1f77bcf86cd799439011");
            return p;
        });

        var req = new CreatePaymentRequest(
                "Pedido #1",
                2,
                "Alice",
                "Bob",
                new BigDecimal("99.50"),
                PaymentStatus.PENDIENTE
        );

        var response = paymentService.create(req);

        assertThat(response.id()).isEqualTo("507f1f77bcf86cd799439011");
        assertThat(response.estatus()).isEqualTo(PaymentStatus.PENDIENTE);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void updateStatus_whenChanged_publishesEvent() {
        Payment existing = new Payment();
        existing.setId("id1");
        existing.setConcepto("X");
        existing.setCantidadProductos(1);
        existing.setPagador("a");
        existing.setBeneficiario("b");
        existing.setMontoTotal(BigDecimal.ONE);
        existing.setEstatus(PaymentStatus.PENDIENTE);
        existing.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        existing.setUpdatedAt(Instant.parse("2025-01-01T00:00:00Z"));

        when(paymentRepository.findById("id1")).thenReturn(Optional.of(existing));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.updateStatus("id1", new UpdatePaymentStatusRequest(PaymentStatus.COMPLETADO));

        ArgumentCaptor<PaymentStatusChangedEvent> captor = ArgumentCaptor.forClass(PaymentStatusChangedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().previousStatus()).isEqualTo(PaymentStatus.PENDIENTE);
        assertThat(captor.getValue().newStatus()).isEqualTo(PaymentStatus.COMPLETADO);
        assertThat(captor.getValue().paymentId()).isEqualTo("id1");
    }

    @Test
    void updateStatus_whenSameStatus_doesNotPublish() {
        Payment existing = new Payment();
        existing.setId("id1");
        existing.setConcepto("X");
        existing.setCantidadProductos(1);
        existing.setPagador("a");
        existing.setBeneficiario("b");
        existing.setMontoTotal(BigDecimal.ONE);
        existing.setEstatus(PaymentStatus.PENDIENTE);
        existing.setCreatedAt(Instant.now());
        existing.setUpdatedAt(Instant.now());

        when(paymentRepository.findById("id1")).thenReturn(Optional.of(existing));

        paymentService.updateStatus("id1", new UpdatePaymentStatusRequest(PaymentStatus.PENDIENTE));

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void getById_throwsWhenMissing() {
        when(paymentRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paymentService.getById("missing"))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}

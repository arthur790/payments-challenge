package com.bancobase.payments.payment.service;

import com.bancobase.payments.messaging.PaymentStatusChangedEvent;
import com.bancobase.payments.messaging.PaymentStatusEventPublisher;
import com.bancobase.payments.payment.exception.PaymentNotFoundException;
import com.bancobase.payments.payment.model.Payment;
import com.bancobase.payments.payment.model.PaymentStatus;
import com.bancobase.payments.payment.dto.CreatePaymentRequest;
import com.bancobase.payments.payment.dto.PaymentResponse;
import com.bancobase.payments.payment.dto.UpdatePaymentStatusRequest;
import com.bancobase.payments.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStatusEventPublisher eventPublisher;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentStatusEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PaymentResponse create(CreatePaymentRequest request) {
        Instant now = Instant.now();
        Payment payment = new Payment();
        payment.setConcepto(request.concepto());
        payment.setCantidadProductos(request.cantidadProductos());
        payment.setPagador(request.pagador());
        payment.setBeneficiario(request.beneficiario());
        payment.setMontoTotal(request.montoTotal());
        payment.setEstatus(request.estatus());
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse getById(String id) {
        return paymentRepository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Override
    public PaymentResponse updateStatus(String id, UpdatePaymentStatusRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        PaymentStatus previous = payment.getEstatus();
        PaymentStatus next = request.estatus();
        if (previous == next) {
            return PaymentResponse.from(payment);
        }
        payment.setEstatus(next);
        payment.setUpdatedAt(Instant.now());
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(new PaymentStatusChangedEvent(
                saved.getId(),
                previous,
                next,
                Instant.now(),
                saved.getConcepto()
        ));
        return PaymentResponse.from(saved);
    }
}

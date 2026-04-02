package com.bancobase.payments.payment.service;

import com.bancobase.payments.messaging.PaymentStatusChangedEvent;
import com.bancobase.payments.messaging.PaymentStatusEventPublisher;
import com.bancobase.payments.payment.Payment;
import com.bancobase.payments.payment.PaymentNotFoundException;
import com.bancobase.payments.payment.PaymentStatus;
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
        Payment p = new Payment();
        p.setConcepto(request.concepto());
        p.setCantidadProductos(request.cantidadProductos());
        p.setPagador(request.pagador());
        p.setBeneficiario(request.beneficiario());
        p.setMontoTotal(request.montoTotal());
        p.setEstatus(request.estatus());
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        return PaymentResponse.from(paymentRepository.save(p));
    }

    @Override
    public PaymentResponse getById(String id) {
        return paymentRepository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Override
    public PaymentResponse updateStatus(String id, UpdatePaymentStatusRequest request) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        PaymentStatus previous = p.getEstatus();
        PaymentStatus next = request.estatus();
        if (previous == next) {
            return PaymentResponse.from(p);
        }
        p.setEstatus(next);
        p.setUpdatedAt(Instant.now());
        Payment saved = paymentRepository.save(p);
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

package com.bancobase.payments.payment.controller;

import com.bancobase.payments.payment.dto.CreatePaymentRequest;
import com.bancobase.payments.payment.dto.PaymentResponse;
import com.bancobase.payments.payment.dto.UpdatePaymentStatusRequest;
import com.bancobase.payments.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.create(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable String id) {
        return paymentService.getById(id);
    }

    @PatchMapping("/{id}/status")
    public PaymentResponse updateStatus(@PathVariable String id, @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return paymentService.updateStatus(id, request);
    }
}

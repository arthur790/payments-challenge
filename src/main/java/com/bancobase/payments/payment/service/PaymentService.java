package com.bancobase.payments.payment.service;

import com.bancobase.payments.payment.dto.CreatePaymentRequest;
import com.bancobase.payments.payment.dto.PaymentResponse;
import com.bancobase.payments.payment.dto.UpdatePaymentStatusRequest;

public interface PaymentService {

    PaymentResponse create(CreatePaymentRequest request);

    PaymentResponse getById(String id);

    PaymentResponse updateStatus(String id, UpdatePaymentStatusRequest request);
}

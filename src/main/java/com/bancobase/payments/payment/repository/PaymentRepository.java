package com.bancobase.payments.payment.repository;

import com.bancobase.payments.payment.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}

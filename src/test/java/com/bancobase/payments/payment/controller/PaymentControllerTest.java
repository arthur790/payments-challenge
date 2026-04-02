package com.bancobase.payments.payment.controller;

import com.bancobase.payments.payment.PaymentStatus;
import com.bancobase.payments.payment.dto.CreatePaymentRequest;
import com.bancobase.payments.payment.dto.PaymentResponse;
import com.bancobase.payments.payment.dto.UpdatePaymentStatusRequest;
import com.bancobase.payments.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void post_createsPayment() throws Exception {
        var body = new CreatePaymentRequest(
                "Test",
                1,
                "p1",
                "p2",
                new BigDecimal("10.00"),
                PaymentStatus.PENDIENTE
        );
        var response = new PaymentResponse(
                "id1",
                "Test",
                1,
                "p1",
                "p2",
                new BigDecimal("10.00"),
                PaymentStatus.PENDIENTE,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z")
        );
        when(paymentService.create(any(CreatePaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("id1"))
                .andExpect(jsonPath("$.estatus").value("PENDIENTE"));
    }

    @Test
    void get_returnsPayment() throws Exception {
        when(paymentService.getById("id1")).thenReturn(new PaymentResponse(
                "id1",
                "X",
                1,
                "a",
                "b",
                BigDecimal.ONE,
                PaymentStatus.COMPLETADO,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z")
        ));

        mockMvc.perform(get("/api/payments/id1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("COMPLETADO"));
    }

    @Test
    void patch_updatesStatus() throws Exception {
        var req = new UpdatePaymentStatusRequest(PaymentStatus.CANCELADO);
        when(paymentService.updateStatus(eq("id1"), any(UpdatePaymentStatusRequest.class)))
                .thenReturn(new PaymentResponse(
                        "id1",
                        "X",
                        1,
                        "a",
                        "b",
                        BigDecimal.ONE,
                        PaymentStatus.CANCELADO,
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-01-02T00:00:00Z")
                ));

        mockMvc.perform(patch("/api/payments/id1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("CANCELADO"));
    }
}

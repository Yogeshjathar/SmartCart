package com.smartcart.payment.controller;

import com.smartcart.common.exception.ErrorCode;
import com.smartcart.common.exception.ResourceNotFoundException;
import com.smartcart.payment.entity.Payment;
import com.smartcart.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping("/order/{orderId}")
    public Payment getPayment(@PathVariable UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for orderId: " + orderId,
                        ErrorCode.PAYMENT_NOT_FOUND
                ));
    }
}

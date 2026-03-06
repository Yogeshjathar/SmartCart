package com.smartcart.payment.consumer;

import com.smartcart.payment.entity.Payment;
import com.smartcart.payment.entity.PaymentStatus;
import com.smartcart.payment.event.PaymentFailedEvent;
import com.smartcart.payment.event.PaymentInitiatedEvent;
import com.smartcart.payment.event.PaymentSuccessEvent;
import com.smartcart.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment-initiate-topic", groupId = "payment-service-group")
    public void handlePaymentInitiation(PaymentInitiatedEvent event) {

        log.info("Processing payment for order {}", event.getOrderId());

        // Idempotency check
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("Payment already processed for order {}", event.getOrderId());
            return;
        }

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .status(PaymentStatus.PROCESSING)
                .transactionReference("TXN-" + UUID.randomUUID())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        payment = paymentRepository.save(payment);

        // Simulate gateway processing
        boolean success = simulatePaymentGateway();

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            kafkaTemplate.send("payment-success-topic",
                    PaymentSuccessEvent.builder()
                            .userId(event.getUserId())
                            .orderId(event.getOrderId())
                            .build());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            kafkaTemplate.send("payment-failed-topic",
                    PaymentFailedEvent.builder()
                            .orderId(event.getOrderId())
                            .build());
        }

        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);
    }

    private boolean simulatePaymentGateway() {
        return Math.random() > 0.2; // 80% success rate
    }
}

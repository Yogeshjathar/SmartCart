package com.smartcart.payment.consumer;

import com.smartcart.common.event.InventoryReservedEvent;
import com.smartcart.common.kafka.KafkaTopics;
import com.smartcart.common.util.KafkaTraceUtil;
import com.smartcart.payment.entity.Payment;
import com.smartcart.payment.entity.PaymentStatus;
import com.smartcart.payment.mapper.EventMapper;
import com.smartcart.payment.producer.EventProducer;
import com.smartcart.payment.repository.PaymentRepository;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;
    private final EventMapper eventMapper;
    private final EventProducer eventProducer;
    private final Tracer tracer;

    @KafkaListener(
            topics = KafkaTopics.INVENTORY_RESERVED,
            groupId = "payment-service",
            containerFactory = "inventoryReservedKafkaListenerContainerFactory"
    )
    public void handleInventoryReserved(ConsumerRecord<String, InventoryReservedEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "payment.inventory-reserved", () -> {
            InventoryReservedEvent event = record.value();

            log.info(
                    "Processing payment for reserved inventory | orderId={} | partition={} | offset={}",
                    event.getOrderId(),
                    record.partition(),
                    record.offset()
            );

            UUID orderId = UUID.fromString(event.getOrderId());

            if (paymentRepository.findByOrderId(orderId).isPresent()) {
                log.info("Payment already processed for order {}", event.getOrderId());
                return;
            }

            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .amount(event.getTotalAmount())
                    .currency(event.getCurrency())
                    .status(PaymentStatus.PROCESSING)
                    .transactionReference("TXN-" + UUID.randomUUID())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            payment = paymentRepository.save(payment);

            boolean success = simulatePaymentGateway();

            if (success) {
                payment.setStatus(PaymentStatus.SUCCESS);
                eventProducer.publish(eventMapper.buildPaymentSuccessEvent(event));
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                eventProducer.publish(eventMapper.buildPaymentFailedEvent(event, "PAYMENT_GATEWAY_DECLINED"));
            }

            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);
        });
    }

    private boolean simulatePaymentGateway() {
        return Math.random() > 0.2; // 80% success rate
    }
}

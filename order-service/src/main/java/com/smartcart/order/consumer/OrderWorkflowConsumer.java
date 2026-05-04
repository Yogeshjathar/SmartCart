package com.smartcart.order.consumer;

import com.smartcart.common.event.InventoryReservationFailedEvent;
import com.smartcart.common.event.InventoryReservedEvent;
import com.smartcart.common.event.PaymentFailedEvent;
import com.smartcart.common.event.PaymentSuccessEvent;
import com.smartcart.common.kafka.KafkaTopics;
import com.smartcart.common.util.KafkaTraceUtil;
import com.smartcart.common.util.TraceUtil;
import com.smartcart.order.entity.Order;
import com.smartcart.order.entity.OrderStatus;
import com.smartcart.order.entity.PaymentStatus;
import com.smartcart.order.repository.OrderRepository;
import io.micrometer.tracing.Tracer;
import jakarta.transaction.Transactional;
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
public class OrderWorkflowConsumer {

    private final OrderRepository orderRepository;
    private final Tracer tracer;

    @KafkaListener(
            topics = KafkaTopics.INVENTORY_RESERVED,
            groupId = "order-service",
            containerFactory = "inventoryReservedKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryReserved(ConsumerRecord<String, InventoryReservedEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "order.workflow.inventory-reserved", () -> {
            InventoryReservedEvent event = record.value();
            Order order = getOrder(event.getOrderId());

            order.setStatus(OrderStatus.RESERVED);
            order.setPaymentStatus(PaymentStatus.INITIATED);
            applyWorkflowMetadata(order, event.getCorrelationId(), TraceUtil.getTraceId(), TraceUtil.getSpanId(), event.getEventId(), event.getEventType().name(), event.getSource());
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);

            log.info("Order updated after inventory reservation | orderId={}", event.getOrderId());
        });
    }

    @KafkaListener(
            topics = KafkaTopics.INVENTORY_RESERVATION_FAILED,
            groupId = "order-service",
            containerFactory = "inventoryReservationFailedKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryReservationFailed(ConsumerRecord<String, InventoryReservationFailedEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "order.workflow.inventory-failed", () -> {
            InventoryReservationFailedEvent event = record.value();
            Order order = getOrder(event.getOrderId());

            order.setStatus(OrderStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.NOT_STARTED);
            applyWorkflowMetadata(order, event.getCorrelationId(), TraceUtil.getTraceId(), TraceUtil.getSpanId(), event.getEventId(), event.getEventType().name(), event.getSource());
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);

            log.info("Order failed after inventory reservation failure | orderId={} | reason={}", event.getOrderId(), event.getReason());
        });
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCESS,
            groupId = "order-service",
            containerFactory = "paymentSuccessKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentSuccess(ConsumerRecord<String, PaymentSuccessEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "order.workflow.payment-success", () -> {
            PaymentSuccessEvent event = record.value();
            Order order = getOrder(event.getOrderId());

            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            applyWorkflowMetadata(order, event.getCorrelationId(), TraceUtil.getTraceId(), TraceUtil.getSpanId(), event.getEventId(), event.getEventType().name(), event.getSource());
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);

            log.info("Order confirmed after payment success | orderId={}", event.getOrderId());
        });
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "order-service",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentFailed(ConsumerRecord<String, PaymentFailedEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "order.workflow.payment-failed", () -> {
            PaymentFailedEvent event = record.value();
            Order order = getOrder(event.getOrderId());

            order.setStatus(OrderStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
            applyWorkflowMetadata(order, event.getCorrelationId(), TraceUtil.getTraceId(), TraceUtil.getSpanId(), event.getEventId(), event.getEventType().name(), event.getSource());
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);

            log.info("Order failed after payment failure | orderId={} | reason={}", event.getOrderId(), event.getReason());
        });
    }

    private Order getOrder(String orderId) {
        return orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new RuntimeException("Order not found for workflow event: " + orderId));
    }

    private void applyWorkflowMetadata(
            Order order,
            String correlationId,
            String traceId,
            String spanId,
            String eventId,
            String eventType,
            String source
    ) {
        order.setCorrelationId(correlationId);
        order.setTraceId(traceId);
        order.setLastSpanId(spanId);
        order.setLastEventId(eventId);
        order.setLastEventType(eventType);
        order.setLastEventSource(source);
        order.setWorkflowUpdatedAt(Instant.now());
    }
}

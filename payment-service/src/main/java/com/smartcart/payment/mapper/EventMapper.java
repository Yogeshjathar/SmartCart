package com.smartcart.payment.mapper;

import com.smartcart.common.event.AggregateType;
import com.smartcart.common.event.EventType;
import com.smartcart.common.event.InventoryReservedEvent;
import com.smartcart.common.event.PaymentFailedEvent;
import com.smartcart.common.event.PaymentSuccessEvent;
import com.smartcart.common.util.TraceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventMapper {

    @Value("${spring.application.name}")
    private String serviceName;

    public PaymentSuccessEvent buildPaymentSuccessEvent(InventoryReservedEvent inventoryEvent) {
        return PaymentSuccessEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EventType.PAYMENT_SUCCESS)
                .aggregateId(inventoryEvent.getOrderId())
                .aggregateType(AggregateType.ORDER)
                .occurredAt(Instant.now())
                .traceId(inventoryEvent.getTraceId())
                .spanId(TraceUtil.getSpanId())
                .version("1.0")
                .correlationId(resolveCorrelationId(inventoryEvent))
                .source(serviceName)
                .orderId(inventoryEvent.getOrderId())
                .userId(inventoryEvent.getUserId())
                .items(inventoryEvent.getItems())
                .totalAmount(inventoryEvent.getTotalAmount())
                .currency(inventoryEvent.getCurrency())
                .build();
    }

    public PaymentFailedEvent buildPaymentFailedEvent(InventoryReservedEvent inventoryEvent, String reason) {
        return PaymentFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EventType.PAYMENT_FAILED)
                .aggregateId(inventoryEvent.getOrderId())
                .aggregateType(AggregateType.ORDER)
                .occurredAt(Instant.now())
                .traceId(inventoryEvent.getTraceId())
                .spanId(TraceUtil.getSpanId())
                .version("1.0")
                .correlationId(resolveCorrelationId(inventoryEvent))
                .source(serviceName)
                .orderId(inventoryEvent.getOrderId())
                .userId(inventoryEvent.getUserId())
                .items(inventoryEvent.getItems())
                .totalAmount(inventoryEvent.getTotalAmount())
                .currency(inventoryEvent.getCurrency())
                .reason(reason)
                .build();
    }

    private String resolveCorrelationId(InventoryReservedEvent inventoryEvent) {
        return inventoryEvent.getCorrelationId() != null
                ? inventoryEvent.getCorrelationId()
                : inventoryEvent.getOrderId();
    }
}

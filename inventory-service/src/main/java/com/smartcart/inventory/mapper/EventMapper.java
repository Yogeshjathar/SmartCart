package com.smartcart.inventory.mapper;

import com.smartcart.common.event.AggregateType;
import com.smartcart.common.event.EventType;
import com.smartcart.common.event.InventoryReservationFailedEvent;
import com.smartcart.common.event.InventoryReservedEvent;
import com.smartcart.common.event.OrderCreatedEvent;
import com.smartcart.common.util.TraceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventMapper {

    @Value("${spring.application.name}")
    private String serviceName;

    public InventoryReservedEvent buildInventoryReservedEvent(OrderCreatedEvent orderEvent) {
        return InventoryReservedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EventType.INVENTORY_RESERVED)
                .aggregateId(orderEvent.getOrderId())
                .aggregateType(AggregateType.ORDER)
                .occurredAt(Instant.now())
                .traceId(resolveTraceId(orderEvent.getTraceId()))
                .spanId(TraceUtil.getSpanId())
                .version("1.0")
                .correlationId(resolveCorrelationId(orderEvent.getCorrelationId(), orderEvent.getOrderId()))
                .source(serviceName)
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .items(orderEvent.getItems())
                .totalAmount(orderEvent.getTotalAmount())
                .currency(orderEvent.getCurrency())
                .build();
    }

    public InventoryReservationFailedEvent buildInventoryReservationFailedEvent(OrderCreatedEvent orderEvent, String reason) {
        return InventoryReservationFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EventType.INVENTORY_RESERVATION_FAILED)
                .aggregateId(orderEvent.getOrderId())
                .aggregateType(AggregateType.ORDER)
                .occurredAt(Instant.now())
                .traceId(resolveTraceId(orderEvent.getTraceId()))
                .spanId(TraceUtil.getSpanId())
                .version("1.0")
                .correlationId(resolveCorrelationId(orderEvent.getCorrelationId(), orderEvent.getOrderId()))
                .source(serviceName)
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .items(orderEvent.getItems())
                .totalAmount(orderEvent.getTotalAmount())
                .currency(orderEvent.getCurrency())
                .reason(reason)
                .build();
    }

    private String resolveTraceId(String traceId) {
        return traceId != null ? traceId : TraceUtil.getTraceId();
    }

    private String resolveCorrelationId(String correlationId, String orderId) {
        if (correlationId != null) {
            return correlationId;
        }

        String fromMdc = TraceUtil.getCorrelationId();
        return fromMdc != null ? fromMdc : orderId;
    }
}

package com.smartcart.order.mapper;

import com.smartcart.common.event.*;
import com.smartcart.common.util.TraceUtil;
import com.smartcart.order.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventMapper {

    @Value("${spring.application.name}")
    private String serviceName;

    public OrderCreatedEvent buildOrderCreatedEvent(Order order) {
        return OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EventType.ORDER_CREATED)
                .aggregateId(order.getId().toString())
                .aggregateType(AggregateType.ORDER)
                .occurredAt(Instant.now())
                .traceId(TraceUtil.getTraceId())
                .spanId(TraceUtil.getSpanId())
                .version("1.0")
                .correlationId(TraceUtil.resolveCorrelationId(order.getCorrelationId()))
                .source(serviceName)
                .orderId(order.getId().toString())
                .userId(order.getUserId())
                .currency(order.getCurrency())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(i -> OrderItemPayload.builder()
                                .productId(i.getProductId())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .build())
                        .toList())
                .build();
    }

    public OrderCancelledEvent buildOrderCancelledEvent(Order order) {
        return OrderCancelledEvent.builder()
                .eventType(EventType.ORDER_CANCELLED)
                .aggregateId(order.getId().toString())
                .aggregateType(AggregateType.ORDER)
                .traceId(TraceUtil.getTraceId())
                .spanId(TraceUtil.getSpanId())
                .version("1.0")
                .correlationId(TraceUtil.resolveCorrelationId(order.getCorrelationId()))
                .source(serviceName)
                .orderId(order.getId().toString())
                .userId(order.getUserId())
                .currency(order.getCurrency())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(i -> OrderItemPayload.builder()
                                .productId(i.getProductId())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .build())
                        .toList())
                .reason("USER_CANCELLED")
                .build();
    }
}

package com.smartcart.order.dto;

import com.smartcart.order.entity.OrderStatus;
import com.smartcart.order.entity.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class OrderWorkflowResponse {
    UUID orderId;
    String userId;
    OrderStatus orderStatus;
    PaymentStatus paymentStatus;
    BigDecimal totalAmount;
    String currency;
    String correlationId;
    String traceId;
    String lastSpanId;
    String lastEventId;
    String lastEventType;
    String lastEventSource;
    Instant createdAt;
    Instant updatedAt;
    Instant workflowUpdatedAt;
}

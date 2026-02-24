package com.smartcart.common.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@SuperBuilder
public class OrderCreatedEvent extends BaseEvent {

    private final String orderId;
    private final String userId;
    private final List<OrderItemPayload> items;
    private final BigDecimal totalAmount;
    private final String currency;
}

package com.smartcart.common.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class OrderCreatedEvent extends BaseEvent {

    private final Long orderId;
    private final double amount;
}

package com.smartcart.common.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class InventoryUpdatedEvent extends BaseEvent {

    private final Long productId;
    private final int quantity;
}

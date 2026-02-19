package com.smartcart.common.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class InventoryUpdatedEvent extends BaseEvent {

    private final String productId;
    private final Integer previousQuantity;
    private final Integer updatedQuantity;
    private final String reason; // ORDER_PLACED, RESTOCK, MANUAL_ADJUSTMENT
}

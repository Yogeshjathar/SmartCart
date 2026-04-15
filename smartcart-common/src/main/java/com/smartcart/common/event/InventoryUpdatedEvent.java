package com.smartcart.common.event;

import com.smartcart.common.kafka.KafkaTopics;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class InventoryUpdatedEvent extends BaseEvent {

    private final String productId;
    private final Integer previousQuantity;
    private final Integer updatedQuantity;
    private final String reason; // ORDER_PLACED, RESTOCK, MANUAL_ADJUSTMENT

    @Override
    public String getTopic() {
        return KafkaTopics.INVENTORY_UPDATED;
    }
}

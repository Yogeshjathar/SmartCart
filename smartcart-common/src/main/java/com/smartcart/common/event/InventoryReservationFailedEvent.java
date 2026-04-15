package com.smartcart.common.event;

import com.smartcart.common.kafka.KafkaTopics;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.List;

@Getter
@SuperBuilder
@Jacksonized
public class InventoryReservationFailedEvent extends BaseEvent {

    private final String orderId;
    private final String userId;
    private final List<OrderItemPayload> items;
    private final BigDecimal totalAmount;
    private final String currency;
    private final String reason;

    @Override
    public String getTopic() {
        return KafkaTopics.INVENTORY_RESERVATION_FAILED;
    }
}

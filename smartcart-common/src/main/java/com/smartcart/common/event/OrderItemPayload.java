package com.smartcart.common.event;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Getter
@Builder
@Jacksonized
public class OrderItemPayload {

    private final String productId;
    private final Integer quantity;
    private final BigDecimal price;
}

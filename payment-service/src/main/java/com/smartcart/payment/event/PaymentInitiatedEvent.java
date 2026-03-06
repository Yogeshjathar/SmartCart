package com.smartcart.payment.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInitiatedEvent {
    private String userId;
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
}

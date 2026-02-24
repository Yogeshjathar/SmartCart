package com.smartcart.notification.event;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentFailedEvent {

    private UUID orderId;
    private String userId;
}
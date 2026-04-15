package com.smartcart.notification.consumer;

import com.smartcart.common.kafka.KafkaTopics;
import com.smartcart.notification.entity.NotificationChannel;
import com.smartcart.notification.entity.NotificationType;
import com.smartcart.notification.event.OrderCreatedEvent;
import com.smartcart.notification.event.PaymentFailedEvent;
import com.smartcart.notification.event.PaymentSuccessEvent;
import com.smartcart.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "notification-group")
    public void handleOrderCreated(OrderCreatedEvent event) {

        notificationService.createAndSend(
                event.getOrderId(),
                event.getUserId(),
                NotificationType.ORDER_CREATED,
                NotificationChannel.EMAIL,
                "Your order has been created successfully."
        );
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_SUCCESS, groupId = "notification-group")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {

        notificationService.createAndSend(
                event.getOrderId(),
                event.getUserId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationChannel.EMAIL,
                "Payment successful. Order confirmed!"
        );
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "notification-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {

        notificationService.createAndSend(
                event.getOrderId(),
                event.getUserId(),
                NotificationType.PAYMENT_FAILED,
                NotificationChannel.EMAIL,
                "Payment failed. Please retry."
        );
    }
}
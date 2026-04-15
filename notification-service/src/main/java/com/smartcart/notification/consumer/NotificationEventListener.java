package com.smartcart.notification.consumer;

import com.smartcart.common.event.OrderCreatedEvent;
import com.smartcart.common.kafka.KafkaTopics;
import com.smartcart.notification.entity.NotificationChannel;
import com.smartcart.notification.entity.NotificationType;
import com.smartcart.notification.event.PaymentFailedEvent;
import com.smartcart.notification.event.PaymentSuccessEvent;
import com.smartcart.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "notification-group",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void handleOrderCreated(ConsumerRecord<String, OrderCreatedEvent> record) {
        OrderCreatedEvent event = record.value();

        log.info(
                "Received ORDER_CREATED event | orderId={} | userId={} | partition={} | offset={}",
                event.getOrderId(),
                event.getUserId(),
                record.partition(),
                record.offset()
        );

        notificationService.createAndSend(
                UUID.fromString(event.getOrderId()),
                event.getUserId(),
                NotificationType.ORDER_CREATED,
                NotificationChannel.EMAIL,
                "Your order has been created successfully."
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCESS,
            groupId = "notification-group",
            containerFactory = "paymentSuccessKafkaListenerContainerFactory"
    )
    public void handlePaymentSuccess(ConsumerRecord<String, PaymentSuccessEvent> record) {
        PaymentSuccessEvent event = record.value();

        notificationService.createAndSend(
                event.getOrderId(),
                event.getUserId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationChannel.EMAIL,
                "Payment successful. Order confirmed!"
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "notification-group",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(ConsumerRecord<String, PaymentFailedEvent> record) {
        PaymentFailedEvent event = record.value();

        notificationService.createAndSend(
                event.getOrderId(),
                event.getUserId(),
                NotificationType.PAYMENT_FAILED,
                NotificationChannel.EMAIL,
                "Payment failed. Please retry."
        );
    }
}

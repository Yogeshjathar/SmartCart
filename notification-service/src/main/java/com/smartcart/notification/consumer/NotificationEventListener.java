package com.smartcart.notification.consumer;

import com.smartcart.common.event.OrderCreatedEvent;
import com.smartcart.common.event.PaymentFailedEvent;
import com.smartcart.common.event.PaymentSuccessEvent;
import com.smartcart.common.kafka.KafkaTopics;
import com.smartcart.common.util.KafkaTraceUtil;
import com.smartcart.notification.entity.NotificationChannel;
import com.smartcart.notification.entity.NotificationType;
import com.smartcart.notification.service.NotificationService;
import io.micrometer.tracing.Tracer;
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
    private final Tracer tracer;

    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "notification-group",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void handleOrderCreated(ConsumerRecord<String, OrderCreatedEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "notification.order-created", () -> {
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
        });
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCESS,
            groupId = "notification-group",
            containerFactory = "paymentSuccessKafkaListenerContainerFactory"
    )
    public void handlePaymentSuccess(ConsumerRecord<String, PaymentSuccessEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "notification.payment-success", () -> {
            PaymentSuccessEvent event = record.value();

            notificationService.createAndSend(
                    UUID.fromString(event.getOrderId()),
                    event.getUserId(),
                    NotificationType.PAYMENT_SUCCESS,
                    NotificationChannel.EMAIL,
                    "Payment successful. Order confirmed!"
            );
        });
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "notification-group",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(ConsumerRecord<String, PaymentFailedEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "notification.payment-failed", () -> {
            PaymentFailedEvent event = record.value();

            notificationService.createAndSend(
                    UUID.fromString(event.getOrderId()),
                    event.getUserId(),
                    NotificationType.PAYMENT_FAILED,
                    NotificationChannel.EMAIL,
                    "Payment failed. Please retry."
            );
        });
    }
}

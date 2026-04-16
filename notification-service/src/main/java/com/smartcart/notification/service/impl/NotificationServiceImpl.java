package com.smartcart.notification.service.impl;

import com.smartcart.notification.entity.NotificationChannel;
import com.smartcart.notification.entity.NotificationStatus;
import com.smartcart.notification.entity.NotificationType;
import com.smartcart.notification.repository.NotificationRepository;
import com.smartcart.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    @Override
    public void createAndSend(UUID orderId,
                              String userId,
                              NotificationType type,
                              NotificationChannel channel,
                              String message) {

        log.info(
                "Creating notification | orderId={} | userId={} | type={} | channel={}",
                orderId,
                userId,
                type,
                channel
        );

        com.smartcart.notification.model.Notification notification = com.smartcart.notification.model.Notification.builder()
                .orderId(orderId)
                .userId(userId)
                .type(type)
                .channel(channel)
                .status(NotificationStatus.PENDING)
                .message(message)
                .recipient(resolveRecipient(userId, channel))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        repository.save(notification);
        log.info("Notification saved with status={} for orderId={}", notification.getStatus(), orderId);

        try {
            simulateSending(channel, notification.getRecipient(), message);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            log.info("Notification sent successfully for orderId={}", orderId);

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("Notification failed for order {}", orderId, e);
        }

        notification.setUpdatedAt(Instant.now());
        repository.save(notification);
    }

    private void simulateSending(NotificationChannel channel,
                                 String recipient,
                                 String message) {

        log.info("Sending {} to {}: {}", channel, recipient, message);

        if (Math.random() < 0.1) {
            throw new RuntimeException("Simulated failure");
        }
    }

    private String resolveRecipient(String userId, NotificationChannel channel) {

        return switch (channel) {
            case EMAIL -> userId + "@smartcart.com";
            case SMS -> "+911234567890";
            case PUSH -> "device-token-" + userId;
        };
    }
}

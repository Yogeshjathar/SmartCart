package com.smartcart.notification.model;

import com.smartcart.notification.entity.NotificationChannel;
import com.smartcart.notification.entity.NotificationStatus;
import com.smartcart.notification.entity.NotificationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    private UUID orderId;
    private String userId;

    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;

    private String message;
    private String recipient;

    private Instant createdAt;
    private Instant sentAt;
    private Instant updatedAt;
}
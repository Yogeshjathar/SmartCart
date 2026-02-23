package com.smartcart.notification.service;


import com.smartcart.notification.entity.NotificationChannel;
import com.smartcart.notification.entity.NotificationType;

import java.util.UUID;

public interface NotificationService {

    void createAndSend(UUID orderId,
                       String userId,
                       NotificationType type,
                       NotificationChannel channel,
                       String message);
}
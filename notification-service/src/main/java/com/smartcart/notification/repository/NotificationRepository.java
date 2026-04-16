package com.smartcart.notification.repository;

import com.smartcart.notification.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByOrderId(UUID orderId);

    List<Notification> findByUserId(String userId);
}

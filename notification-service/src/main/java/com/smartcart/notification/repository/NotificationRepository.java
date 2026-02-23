package com.smartcart.notification.repository;

import com.smartcart.notification.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByOrderId(String orderId);

    List<Notification> findByUserId(String userId);
}
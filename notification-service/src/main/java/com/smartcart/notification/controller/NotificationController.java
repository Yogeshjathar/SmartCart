package com.smartcart.notification.controller;

import com.smartcart.notification.model.Notification;
import com.smartcart.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;

    @GetMapping("/order/{orderId}")
    public List<Notification> getByOrder(@PathVariable String orderId) {
        return repository.findByOrderId(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getByUser(@PathVariable String userId) {
        return repository.findByUserId(userId);
    }
}
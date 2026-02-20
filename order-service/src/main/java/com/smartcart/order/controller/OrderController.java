package com.smartcart.order.controller;

import com.smartcart.order.dto.CreateOrderRequest;
import com.smartcart.order.entity.Order;
import com.smartcart.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Order createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable UUID orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable String userId) {
        return orderService.getOrdersByUser(userId);
    }

    @PutMapping("/{orderId}/cancel")
    public Order cancelOrder(@PathVariable UUID orderId) {
        return orderService.cancelOrder(orderId);
    }
}

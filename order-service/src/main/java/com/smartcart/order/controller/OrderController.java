package com.smartcart.order.controller;

import com.smartcart.common.response.ApiResponse;
import com.smartcart.order.dto.CreateOrderRequest;
import com.smartcart.order.dto.OrderWorkflowResponse;
import com.smartcart.order.entity.Order;
import com.smartcart.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
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

    @GetMapping("/{orderId}/workflow")
    public OrderWorkflowResponse getOrderWorkflow(@PathVariable UUID orderId) {
        return orderService.getOrderWorkflow(orderId);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(@PathVariable UUID orderId) {
        Order order = orderService.cancelOrder(orderId);

        String message = order.getStatus() == com.smartcart.order.entity.OrderStatus.CANCELLED
                ? "Order cancellation processed successfully"
                : "Order fetched successfully";

        return ResponseEntity.ok(ApiResponse.success(order, message));
    }
}

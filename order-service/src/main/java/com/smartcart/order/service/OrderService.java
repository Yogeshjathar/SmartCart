package com.smartcart.order.service;

import com.smartcart.order.dto.CreateOrderRequest;
import com.smartcart.order.entity.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order createOrder(CreateOrderRequest request);

    Order getOrder(UUID orderId);

    List<Order> getOrdersByUser(String userId);

    Order cancelOrder(UUID orderId);
}

package com.smartcart.order.service.impl;

import com.smartcart.common.event.OrderCancelledEvent;
import com.smartcart.common.event.OrderCreatedEvent;
import com.smartcart.order.dto.CreateOrderRequest;
import com.smartcart.order.entity.Order;
import com.smartcart.order.entity.OrderItem;
import com.smartcart.order.entity.OrderStatus;
import com.smartcart.order.entity.PaymentStatus;
import com.smartcart.order.mapper.EventMapper;
import com.smartcart.order.producer.EventProducer;
import com.smartcart.order.repository.OrderRepository;
import com.smartcart.order.service.OrderService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final EventProducer eventProducer;
    private final EventMapper eventMapper;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        log.info("Creating order for user {}", request.getUserId());

        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.NOT_STARTED)
                .currency(request.getCurrency())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        final BigDecimal[] total = {BigDecimal.ZERO};

        List<OrderItem> items = request.getItems().stream().map(i -> {
            BigDecimal subtotal = i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            total[0] = total[0].add(subtotal);

            return OrderItem.builder()
                    .productId(i.getProductId())
                    .quantity(i.getQuantity())
                    .price(i.getPrice())
                    .subtotal(subtotal)
                    .order(order)
                    .build();
        }).collect(Collectors.toCollection(ArrayList::new));;

        order.setItems(items);
        order.setTotalAmount(total[0]);

        Order saved = orderRepository.save(order);

        meterRegistry.counter("orders.created").increment();

        OrderCreatedEvent event = eventMapper.buildOrderCreatedEvent(saved);
        eventProducer.publish(event);

        return saved;
    }

    @Override
    public Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {

        Order order = getOrder(orderId);

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new RuntimeException("Cannot cancel confirmed order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());

        Order savedOrder = orderRepository.save(order);

        OrderCancelledEvent event = eventMapper.buildOrderCancelledEvent(savedOrder);
        eventProducer.publish(event);

        return savedOrder;
    }
}

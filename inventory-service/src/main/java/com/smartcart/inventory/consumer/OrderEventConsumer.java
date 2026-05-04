package com.smartcart.inventory.consumer;

import com.smartcart.common.event.OrderItemPayload;
import com.smartcart.common.event.OrderCreatedEvent;
import com.smartcart.common.event.OrderCancelledEvent;
import com.smartcart.common.event.PaymentFailedEvent;
import com.smartcart.common.event.PaymentSuccessEvent;
import com.smartcart.common.kafka.KafkaTopics;
import com.smartcart.common.util.KafkaTraceUtil;
import com.smartcart.inventory.mapper.EventMapper;
import com.smartcart.inventory.producer.EventProducer;
import com.smartcart.inventory.service.InventoryService;
import io.micrometer.tracing.Tracer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final EventMapper eventMapper;
    private final EventProducer eventProducer;
    private final Tracer tracer;

    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "inventory-service",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void handleOrderCreated(ConsumerRecord<String, OrderCreatedEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "inventory.order-created", () -> {
            OrderCreatedEvent event = record.value();

            log.info(
                    "Received ORDER_CREATED event | orderId={} | eventId={} | partition={} | offset={}",
                    event.getOrderId(),
                    event.getEventId(),
                    record.partition(),
                    record.offset()
            );

            int reservedItems = 0;

            try {
                for (OrderItemPayload item : event.getItems()) {
                    log.info(
                            "Reserving stock | productId={} | quantity={}",
                            item.getProductId(),
                            item.getQuantity()
                    );

                    inventoryService.reserveStock(
                            item.getProductId(),
                            item.getQuantity()
                    );

                    reservedItems++;
                }

                eventProducer.publish(eventMapper.buildInventoryReservedEvent(event));

                log.info(
                        "Inventory reserved successfully for created order | orderId={}",
                        event.getOrderId()
                );

            } catch (Exception ex) {
                compensateReservation(event, reservedItems);

                eventProducer.publish(
                        eventMapper.buildInventoryReservationFailedEvent(event, ex.getMessage())
                );

                log.error(
                        "Failed to process ORDER_CREATED event | orderId={} | eventId={}",
                        event.getOrderId(),
                        event.getEventId(),
                        ex
                );
            }
        });
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            groupId = "inventory-service",
            containerFactory = "orderCancelledKafkaListenerContainerFactory"
    )
    public void handleOrderCancelled(ConsumerRecord<String, OrderCancelledEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "inventory.order-cancelled", () -> {
            OrderCancelledEvent event = record.value();

            log.info(
                    "Received ORDER_CANCELLED event | orderId={} | eventId={} | partition={} | offset={}",
                    event.getOrderId(),
                    event.getEventId(),
                    record.partition(),
                    record.offset()
            );

            try {

                for (OrderItemPayload item : event.getItems()) {

                    log.info(
                            "Releasing stock | productId={} | quantity={}",
                            item.getProductId(),
                            item.getQuantity()
                    );

                    inventoryService.releaseStock(
                            item.getProductId(),
                            item.getQuantity()
                    );
                }

                log.info(
                        "Inventory updated successfully for cancelled order | orderId={}",
                        event.getOrderId()
                );

            } catch (Exception ex) {

                log.error(
                        "Failed to process ORDER_CANCELLED event | orderId={} | eventId={}",
                        event.getOrderId(),
                        event.getEventId(),
                        ex
                );

                throw ex;
            }
        });
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCESS,
            groupId = "inventory-service",
            containerFactory = "paymentSuccessKafkaListenerContainerFactory"
    )
    public void handlePaymentSuccess(ConsumerRecord<String, PaymentSuccessEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "inventory.payment-success", () -> {
            PaymentSuccessEvent event = record.value();

            try {
                for (OrderItemPayload item : event.getItems()) {
                    inventoryService.confirmStock(item.getProductId(), item.getQuantity());
                }

                log.info("Inventory confirmed for paid order | orderId={}", event.getOrderId());
            } catch (Exception ex) {
                log.error("Failed to confirm inventory for paid order | orderId={}", event.getOrderId(), ex);
                throw ex;
            }
        });
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "inventory-service",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(ConsumerRecord<String, PaymentFailedEvent> record) {
        KafkaTraceUtil.runWithConsumerSpan(tracer, record, "inventory.payment-failed", () -> {
            PaymentFailedEvent event = record.value();

            try {
                for (OrderItemPayload item : event.getItems()) {
                    inventoryService.releaseStock(item.getProductId(), item.getQuantity());
                }

                log.info("Inventory released after payment failure | orderId={}", event.getOrderId());
            } catch (Exception ex) {
                log.error("Failed to release inventory after payment failure | orderId={}", event.getOrderId(), ex);
                throw ex;
            }
        });
    }

    private void compensateReservation(OrderCreatedEvent event, int reservedItems) {
        for (int index = reservedItems - 1; index >= 0; index--) {
            OrderItemPayload item = event.getItems().get(index);

            try {
                inventoryService.releaseStock(item.getProductId(), item.getQuantity());
            } catch (Exception compensationEx) {
                log.error(
                        "Failed to compensate reserved stock | orderId={} | productId={}",
                        event.getOrderId(),
                        item.getProductId(),
                        compensationEx
                );
            }
        }
    }
}

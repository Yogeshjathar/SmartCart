package com.smartcart.inventory.consumer;

import com.smartcart.common.event.OrderCancelledEvent;
import com.smartcart.common.event.OrderItemPayload;
import com.smartcart.common.kafka.KafkaTopics;
import com.smartcart.inventory.service.InventoryService;

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

    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            groupId = "inventory-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCancelled(ConsumerRecord<String, OrderCancelledEvent> record) {

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

            throw ex; // let Kafka retry
        }
    }
}
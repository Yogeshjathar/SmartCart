package com.smartcart.inventory.service.impl;

import com.smartcart.common.event.InventoryUpdatedEvent;
import com.smartcart.common.exception.ErrorCode;
import com.smartcart.common.exception.ResourceNotFoundException;
import com.smartcart.inventory.dto.InventoryRequest;
import com.smartcart.inventory.entity.Inventory;
import com.smartcart.inventory.repository.InventoryRepository;
import com.smartcart.inventory.service.InventoryService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
//    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    private static final String INVENTORY_TOPIC = "inventory-events";

    @Override
    @Transactional
    public Inventory addStock(InventoryRequest request) {
        log.info("Adding stock: productId={}, quantity={}, traceId={}",
                request.getProductId(), request.getQuantity(), MDC.get("traceId"));

        Inventory inventory = repository.findByProductId(request.getProductId())
                .orElse(Inventory.builder()
                        .productId(request.getProductId())
                        .availableQuantity(0)
                        .reservedQuantity(0)
                        .warehouseLocation(request.getWarehouseLocation())
                        .lastUpdated(Instant.now())
                        .build());

        int previous = inventory.getAvailableQuantity();
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
        inventory.setLastUpdated(Instant.now());

        Inventory saved = repository.save(inventory);

        meterRegistry.counter("inventory.add_stock").increment();

        // Publish event
/*        kafkaTemplate.send(INVENTORY_TOPIC, InventoryUpdatedEvent.builder()
                .productId(saved.getProductId())
                .previousQuantity(previous)
                .updatedQuantity(saved.getAvailableQuantity())
                .reason("RESTOCK")
                .build());*/

        log.info("Stock added: {} → {}, productId={}", previous, saved.getAvailableQuantity(), saved.getProductId());

        return saved;
    }

    @Override
    @Transactional
    public Inventory reserveStock(String productId, int quantity) {

        log.info("Reserving stock: productId={}, quantity={}, traceId={}", productId, quantity, MDC.get("traceId"));

        Inventory inventory = repository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for productId: " + productId, ErrorCode.PRODUCT_NOT_FOUND));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for productId: " + productId);
        }

        int previous = inventory.getAvailableQuantity();
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setLastUpdated(Instant.now());

        Inventory saved = repository.save(inventory);

        meterRegistry.counter("inventory.reserve_stock").increment();

/*        kafkaTemplate.send(INVENTORY_TOPIC, InventoryUpdatedEvent.builder()
                .productId(saved.getProductId())
                .previousQuantity(previous)
                .updatedQuantity(saved.getAvailableQuantity())
                .reason("ORDER_PLACED")
                .build());*/

        return saved;
    }

    @Override
    @Transactional
    public Inventory releaseStock(String productId, int quantity) {

        log.info("Releasing stock: productId={}, quantity={}, traceId={}", productId, quantity, MDC.get("traceId"));

        Inventory inventory = repository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for productId: " + productId, ErrorCode.PRODUCT_NOT_FOUND));

        int previous = inventory.getAvailableQuantity();
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setLastUpdated(Instant.now());

        Inventory saved = repository.save(inventory);

        meterRegistry.counter("inventory.release_stock").increment();

/*        kafkaTemplate.send(INVENTORY_TOPIC, InventoryUpdatedEvent.builder()
                .productId(saved.getProductId())
                .previousQuantity(previous)
                .updatedQuantity(saved.getAvailableQuantity())
                .reason("PAYMENT_FAILED")
                .build());*/

        return saved;
    }

    @Override
    @Transactional
    public Inventory confirmStock(String productId, int quantity) {

        log.info("Confirming stock: productId={}, quantity={}, traceId={}", productId, quantity, MDC.get("traceId"));

        Inventory inventory = repository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for productId: " + productId, ErrorCode.PRODUCT_NOT_FOUND));

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setLastUpdated(Instant.now());

        Inventory saved = repository.save(inventory);

        meterRegistry.counter("inventory.confirm_stock").increment();

/*
        kafkaTemplate.send(INVENTORY_TOPIC, InventoryUpdatedEvent.builder()
                .productId(saved.getProductId())
                .previousQuantity(inventory.getAvailableQuantity())
                .updatedQuantity(saved.getAvailableQuantity())
                .reason("PAYMENT_SUCCESS")
                .build());
*/

        return saved;
    }

    @Override
    public Inventory getInventoryByProductId(String productId) {

        log.info("Fetching inventory: productId={}, traceId={}", productId, MDC.get("traceId"));

        meterRegistry.counter("inventory.fetch").increment();

        return repository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for productId: " + productId, ErrorCode.PRODUCT_NOT_FOUND));
    }
}

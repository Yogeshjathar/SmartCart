package com.smartcart.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class InventoryResponse {

    private UUID id;
    private String productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private String warehouseLocation;
    private Instant lastUpdated;
}
package com.smartcart.inventory.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class InventoryRequest {

    @NotBlank
    private String productId;

    @NotNull
    private Integer quantity;

    private String warehouseLocation;
}


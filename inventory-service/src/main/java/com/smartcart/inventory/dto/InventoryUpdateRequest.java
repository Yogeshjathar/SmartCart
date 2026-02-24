package com.smartcart.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryUpdateRequest {

    @NotBlank
    private String productId;

    @NotNull
    private Integer quantity;
}
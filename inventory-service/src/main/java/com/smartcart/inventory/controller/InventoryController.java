package com.smartcart.inventory.controller;

import com.smartcart.common.response.ApiResponse;
import com.smartcart.inventory.dto.InventoryRequest;
import com.smartcart.inventory.entity.Inventory;
import com.smartcart.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ApiResponse<Inventory> addStock(@Valid @RequestBody InventoryRequest request) {
        return ApiResponse.success(inventoryService.addStock(request), "Stock added successfully");
    }

    @PostMapping("/reserve/{productId}")
    public ApiResponse<Inventory> reserveStock(@PathVariable String productId,
                                               @RequestParam int quantity) {
        return ApiResponse.success(inventoryService.reserveStock(productId, quantity),
                "Stock reserved successfully");
    }

    @PostMapping("/release/{productId}")
    public ApiResponse<Inventory> releaseStock(@PathVariable String productId,
                                               @RequestParam int quantity) {
        return ApiResponse.success(inventoryService.releaseStock(productId, quantity),
                "Stock released successfully");
    }

    @PostMapping("/confirm/{productId}")
    public ApiResponse<Inventory> confirmStock(@PathVariable String productId,
                                               @RequestParam int quantity) {
        return ApiResponse.success(inventoryService.confirmStock(productId, quantity),
                "Stock confirmed successfully");
    }

    @GetMapping("/{productId}")
    public ApiResponse<Inventory> getInventory(@PathVariable String productId) {
        return ApiResponse.success(inventoryService.getInventoryByProductId(productId),
                "Inventory fetched successfully");
    }
}

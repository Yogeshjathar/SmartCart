package com.smartcart.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/v1/inventory/reserve/{productId}")
    void reserveStock(@PathVariable("productId") String productId,
                      @RequestParam("quantity") int quantity);

    @PostMapping("/api/v1/inventory/release/{productId}")
    void releaseStock(@PathVariable("productId") String productId,
                      @RequestParam("quantity") int quantity);
}
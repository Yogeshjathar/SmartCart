package com.smartcart.inventory.service;

import com.smartcart.inventory.dto.InventoryRequest;
import com.smartcart.inventory.entity.Inventory;

public interface InventoryService {

    Inventory addStock(InventoryRequest request);

    Inventory reserveStock(String productId, int quantity);

    Inventory releaseStock(String productId, int quantity);

    Inventory confirmStock(String productId, int quantity);

    Inventory getInventoryByProductId(String productId);
}

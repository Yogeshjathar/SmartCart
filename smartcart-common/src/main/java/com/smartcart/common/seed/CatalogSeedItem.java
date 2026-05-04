package com.smartcart.common.seed;

import java.math.BigDecimal;

public record CatalogSeedItem(
        String productId,
        String name,
        String description,
        BigDecimal price,
        String currency,
        String category,
        String brand,
        int inventoryQuantity,
        String warehouseLocation
) {
}

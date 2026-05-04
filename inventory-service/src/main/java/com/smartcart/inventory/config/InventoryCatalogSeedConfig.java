package com.smartcart.inventory.config;

import com.smartcart.common.seed.CatalogSeedData;
import com.smartcart.inventory.entity.Inventory;
import com.smartcart.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

@Slf4j
@Configuration
public class InventoryCatalogSeedConfig {

    @Bean
    ApplicationRunner inventoryCatalogSeeder(InventoryRepository inventoryRepository) {
        return args -> {
            List<Inventory> missingInventory = CatalogSeedData.items().stream()
                    .filter(item -> inventoryRepository.findByProductId(item.productId()).isEmpty())
                    .map(item -> Inventory.builder()
                            .productId(item.productId())
                            .availableQuantity(item.inventoryQuantity())
                            .reservedQuantity(0)
                            .warehouseLocation(item.warehouseLocation())
                            .lastUpdated(Instant.now())
                            .build())
                    .toList();

            if (missingInventory.isEmpty()) {
                log.info("Inventory seed skipped: stock already exists for all {} seeded products", CatalogSeedData.items().size());
                return;
            }

            inventoryRepository.saveAll(missingInventory);
            log.info("Seeded inventory rows for {} products", missingInventory.size());
        };
    }
}

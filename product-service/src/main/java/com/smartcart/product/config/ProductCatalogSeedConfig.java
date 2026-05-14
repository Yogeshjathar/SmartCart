package com.smartcart.product.config;

import com.smartcart.common.seed.CatalogSeedData;
import com.smartcart.product.entity.Product;
import com.smartcart.product.entity.ProductStatus;
import com.smartcart.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

@Slf4j
@Configuration
public class ProductCatalogSeedConfig {

    @Bean
    ApplicationRunner productCatalogSeeder(ProductRepository productRepository) {
        return args -> {
            List<Product> missingProducts = CatalogSeedData.items().stream()
                    .filter(item -> !productRepository.existsById(item.productId()))
                    .map(item -> Product.builder()
                            .id(item.productId())
                            .sku(generateSeedSku(item.productId()))
                            .name(item.name())
                            .description(item.description())
                            .price(item.price())
                            .currency(item.currency())
                            .category(item.category())
                            .brand(item.brand())
                            .status(ProductStatus.ACTIVE)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build())
                    .toList();

            if (missingProducts.isEmpty()) {
                log.info("Product seed skipped: catalog already contains all {} seeded products", CatalogSeedData.items().size());
                return;
            }

            productRepository.saveAll(missingProducts);
            log.info("Seeded {} products into catalog", missingProducts.size());
        };
    }

    @Bean
    ApplicationRunner legacyProductSkuBackfill(ProductRepository productRepository) {
        return args -> {
            List<Product> missingSkuProducts = productRepository.findAll().stream()
                    .filter(product -> product.getSku() == null || product.getSku().isBlank())
                    .peek(product -> product.setSku(generateLegacySku(product.getId())))
                    .toList();

            if (missingSkuProducts.isEmpty()) {
                return;
            }

            productRepository.saveAll(missingSkuProducts);
            log.info("Backfilled SKU for {} legacy products", missingSkuProducts.size());
        };
    }

    private static String generateSeedSku(String productId) {
        return productId.toUpperCase().replaceAll("[^A-Z0-9]+", "-");
    }

    private static String generateLegacySku(String productId) {
        return "LEGACY-" + productId.toUpperCase().replaceAll("[^A-Z0-9]+", "-");
    }
}

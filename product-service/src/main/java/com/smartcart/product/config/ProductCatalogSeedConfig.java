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
}

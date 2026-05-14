package com.smartcart.product.service.impl;

import com.smartcart.common.exception.ConflictException;
import com.smartcart.common.exception.ErrorCode;
import com.smartcart.common.exception.ResourceNotFoundException;
import com.smartcart.product.dto.CreateProductRequest;
import com.smartcart.product.entity.Product;
import com.smartcart.product.entity.ProductStatus;
import com.smartcart.product.repository.ProductRepository;
import com.smartcart.product.service.ProductService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    private static final String INVENTORY_TOPIC = "inventory-events";

    @Override
    public Product createProduct(CreateProductRequest request) {

        log.info("Creating product: name={}, traceId={}",
                request.getName(), MDC.get("traceId"));

        meterRegistry.counter("product.create.request").increment();

        String normalizedSku = normalizeSku(request.getSku());

        if (repository.existsBySkuIgnoreCase(normalizedSku)) {
            log.warn("Duplicate product SKU detected: {}", normalizedSku);
            throw new ConflictException(
                    String.format("Product SKU already exists: %s", normalizedSku)
            );
        }

        if (repository.existsByNameAndBrand(request.getName(), request.getBrand())) {
            log.warn("Duplicate product detected: {} - {}", request.getName(), request.getBrand());
            throw new ConflictException(
                    String.format("Product already exists: %s (Brand: %s)", request.getName(), request.getBrand())
            );
        }

        Product product = Product.builder()
                .sku(normalizedSku)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .category(request.getCategory())
                .brand(request.getBrand())
                .status(ProductStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Product saved = repository.save(product);

        log.info("Product created successfully with id={}", saved.getId());

        meterRegistry.counter("product.create.success").increment();

        // Optional: Publish Kafka event
//        kafkaTemplate.send(INVENTORY_TOPIC, ProductCreatedEvent.builder()
//                .productId(saved.getId())
//                .name(saved.getName())
//                .brand(saved.getBrand())
//                .price(saved.getPrice())
//                .status(saved.getStatus())
//                .createdAt(saved.getCreatedAt())
//                .build());

        return saved;
    }

    @Override
    public Product getProductById(String id) {

        log.info("Fetching product id={}, traceId={}", id, MDC.get("traceId"));

        meterRegistry.counter("product.fetch.request").increment();

        return repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found id={}, traceId={}", id, MDC.get("traceId"));
                    meterRegistry.counter("product.fetch.notfound").increment();
                    return new ResourceNotFoundException(
                            "Product not found",
                            ErrorCode.PRODUCT_NOT_FOUND);
                });
    }

    @Override
    public List<Product> getAllProducts() {

        log.info("Fetching all products");

        meterRegistry.counter("product.fetch.all").increment();

        return repository.findAll();
    }

    private String normalizeSku(String rawSku) {
        String normalized = rawSku == null
                ? ""
                : rawSku.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        if (normalized.isBlank()) {
            throw new ConflictException("Product SKU is required");
        }

        return normalized;
    }
}

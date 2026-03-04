package com.smartcart.product.controller;

import com.smartcart.common.response.ApiResponse;
import com.smartcart.product.dto.CreateProductRequest;
import com.smartcart.product.entity.Product;
import com.smartcart.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> create(@Valid @RequestBody CreateProductRequest request) {

        Product product = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(product, "Product created successfully")
                );
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> getById(@PathVariable String id) {
        return ApiResponse.success(
                productService.getProductById(id),
                "Product fetched successfully");
    }

    @GetMapping
    public ApiResponse<List<Product>> getAll() {
        return ApiResponse.success(
                productService.getAllProducts(),
                "Products fetched successfully");
    }
}

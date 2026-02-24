package com.smartcart.product.service;

import com.smartcart.product.dto.CreateProductRequest;
import com.smartcart.product.entity.Product;

import java.util.List;

public interface ProductService {

    Product createProduct(CreateProductRequest request);

    Product getProductById(String id);

    List<Product> getAllProducts();
}

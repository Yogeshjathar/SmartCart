package com.smartcart.product.repository;

import com.smartcart.product.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

    boolean existsByNameAndBrand(String name, String brand);
}

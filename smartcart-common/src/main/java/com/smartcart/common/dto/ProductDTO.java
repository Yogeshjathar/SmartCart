package com.smartcart.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductDTO {

    @NotNull
    Long id;

    @NotBlank
    String name;

    @Positive
    double price;

    @NotBlank
    String category;

    boolean available;
}

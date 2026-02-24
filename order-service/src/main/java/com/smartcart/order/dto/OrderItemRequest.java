package com.smartcart.order.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    @NotBlank
    private String productId;

    @NotNull
    private Integer quantity;

    @NotNull
    private BigDecimal price;
}

package com.smartcart.order.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank
    private String userId;

    @NotEmpty
    private List<OrderItemRequest> items;

    private String currency;
}

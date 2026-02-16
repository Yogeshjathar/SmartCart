package com.smartcart.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class OrderDTO {

    @NotNull
    Long id;

    @NotNull
    Long userId;

    List<String> items;

    @Positive
    double totalAmount;

    String status;

    Instant createdAt;
}

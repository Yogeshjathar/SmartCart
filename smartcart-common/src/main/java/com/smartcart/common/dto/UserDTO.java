package com.smartcart.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserDTO {

    @NotNull
    Long id;

    @NotBlank
    String name;

    @Email
    @NotBlank
    String email;

    @NotBlank
    String role;

    @NotBlank
    String status;

    Instant createdAt;
}

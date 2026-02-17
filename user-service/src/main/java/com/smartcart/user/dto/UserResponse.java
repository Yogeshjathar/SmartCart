package com.smartcart.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNo;
    private String email;
    private String role;
    private String status;
    private Instant createdAt;
}

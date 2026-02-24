package com.smartcart.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@Data
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNo;
    private String email;
    private String role;
    private String status;
}

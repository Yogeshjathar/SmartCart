package com.smartcart.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@Data
public class UserAuthDetails  {

    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNo;
    private String email;
    private String password;
    private String role;
    private String status;
}

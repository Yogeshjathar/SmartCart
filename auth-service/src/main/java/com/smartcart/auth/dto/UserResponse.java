package com.smartcart.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {
    private String id;
    private String email;
    private String password;
    private List<String> roles;
}

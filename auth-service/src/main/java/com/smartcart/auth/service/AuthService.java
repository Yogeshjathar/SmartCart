package com.smartcart.auth.service;

import com.smartcart.auth.dto.AuthResponse;
import com.smartcart.auth.dto.LoginRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AuthService {
    public AuthResponse login(LoginRequest request);
}

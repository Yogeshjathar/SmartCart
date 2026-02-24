package com.smartcart.auth.service.impl;

import com.smartcart.auth.client.UserServiceClient;
import com.smartcart.auth.dto.AuthResponse;
import com.smartcart.auth.dto.LoginRequest;
import com.smartcart.auth.service.AuthService;
import com.smartcart.auth.util.JwtTokenUtil;
import com.smartcart.common.dto.UserAuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {

        UserAuthDetails user = userServiceClient.getUserByEmail(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtTokenUtil.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();
    }
}

package com.smartcart.user.service;

import com.smartcart.user.dto.UserRequest;
import com.smartcart.user.dto.UserResponse;

import java.util.Optional;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    Optional<UserResponse> getUserByEmail(String email);
}

package com.smartcart.user.service;

import com.smartcart.common.dto.UserResponse;
import com.smartcart.user.dto.UserRequest;

import java.util.Optional;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    Optional<UserResponse> getUserByEmail(String email);
}

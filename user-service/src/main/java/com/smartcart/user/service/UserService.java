package com.smartcart.user.service;

import com.smartcart.common.dto.UserAuthDetails;
import com.smartcart.common.response.ApiResponse;
import com.smartcart.user.dto.UserRequest;

import java.util.Optional;

public interface UserService {

    ApiResponse<Object> registerUser(UserRequest request);

    Optional<UserAuthDetails> getUserByEmail(String email);
}

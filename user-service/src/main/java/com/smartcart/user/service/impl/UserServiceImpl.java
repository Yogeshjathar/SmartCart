package com.smartcart.user.service.impl;

import com.smartcart.common.dto.UserAuthDetails;
import com.smartcart.common.event.UserCreatedEvent;
import com.smartcart.common.exception.ConflictException;
import com.smartcart.common.exception.ErrorCode;
import com.smartcart.common.exception.ResourceNotFoundException;
import com.smartcart.common.response.ApiResponse;
import com.smartcart.user.mapper.EventMapper;
import com.smartcart.user.dto.UserRequest;
import com.smartcart.user.dto.UserResponse;
import com.smartcart.user.entity.User;
import com.smartcart.user.producer.UserEventProducer;
import com.smartcart.user.repository.UserRepository;
import com.smartcart.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserEventProducer userEventProducer;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public ApiResponse<Object> registerUser(UserRequest request) {

        // ❌ Email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        // ❌ Phone exists
        if (userRepository.existsByPhoneNo(request.getPhoneNo())) {
            throw new ConflictException("Phone number already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNo(request.getPhoneNo())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        UserCreatedEvent event = eventMapper.buildUserCreatedEvent(savedUser);

        // 🔥 Publish Event
        userEventProducer.publish(event);

        UserResponse response = UserResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .phoneNo(savedUser.getPhoneNo())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .build();

        // ✅ Success Response
        return ApiResponse.success(response, "User registered successfully");
    }

    @Override
    public Optional<UserAuthDetails> getUserByEmail(String email) {

        return Optional.ofNullable(userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email,
                                ErrorCode.USER_NOT_FOUND
                        )
                ));
    }
}

package com.smartcart.user.controller;

import com.smartcart.common.dto.UserAuthDetails;
import com.smartcart.common.response.ApiResponse;
import com.smartcart.user.dto.UserRequest;
import com.smartcart.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody UserRequest request) {

        return ResponseEntity.ok(userService.registerUser(request));
    }


    @GetMapping
    public ResponseEntity<Optional<UserAuthDetails>> getUserByEmail(
            @RequestParam String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

}

package com.smartcart.auth.client;

import com.smartcart.auth.config.AppConfig;
import com.smartcart.auth.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", configuration = AppConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users")
    UserResponse getUserByEmail(@RequestParam("email") String email);
}

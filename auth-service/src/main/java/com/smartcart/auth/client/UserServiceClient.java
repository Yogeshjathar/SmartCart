package com.smartcart.auth.client;

import com.smartcart.common.dto.UserAuthDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users")
    UserAuthDetails getUserByEmail(@RequestParam("email") String email);
}

package com.learnify.enrollment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface UserClient {

    @GetMapping("/api/auth/user-id")
    Long getUserIdByEmail(@RequestParam("email") String email);
}
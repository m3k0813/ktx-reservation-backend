package com.ktcloudinfra.reservationservice.client;

import com.ktcloudinfra.reservationservice.client.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/api/v1/users/{userId}")
    UserDTO getUser(@PathVariable Long userId);
}

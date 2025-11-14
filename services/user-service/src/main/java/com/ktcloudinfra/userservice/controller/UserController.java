package com.ktcloudinfra.userservice.controller;

import com.ktcloudinfra.userservice.dto.request.CreateUserRequestDTO;
import com.ktcloudinfra.userservice.dto.request.LoginRequestDTO;
import com.ktcloudinfra.userservice.entity.User;
import com.ktcloudinfra.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signup(@RequestBody CreateUserRequestDTO request) {
        userService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("회원가입 완료");
    }

    @PostMapping("/login")
    public ResponseEntity<Long> login(@RequestBody LoginRequestDTO request) {
        Long userId = userService.login(request);
        return ResponseEntity.ok(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }
}

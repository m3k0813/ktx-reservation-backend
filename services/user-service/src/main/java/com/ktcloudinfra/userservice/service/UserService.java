package com.ktcloudinfra.userservice.service;

import com.ktcloudinfra.userservice.dto.request.CreateUserRequestDTO;
import com.ktcloudinfra.userservice.dto.request.LoginRequestDTO;
import com.ktcloudinfra.userservice.entity.User;

public interface UserService {
    void signUp(CreateUserRequestDTO request);

    Long login(LoginRequestDTO request);

    User getUser(Long userId);
}

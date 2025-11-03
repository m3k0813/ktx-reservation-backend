package com.ktcloudinfra.ktx_reservation.domain.user.service;

import com.ktcloudinfra.ktx_reservation.domain.user.dto.request.CreateUserRequestDTO;
import com.ktcloudinfra.ktx_reservation.domain.user.dto.request.LoginRequestDTO;

public interface UserService {
    void signUp(CreateUserRequestDTO request);

    Long login(LoginRequestDTO request);
}

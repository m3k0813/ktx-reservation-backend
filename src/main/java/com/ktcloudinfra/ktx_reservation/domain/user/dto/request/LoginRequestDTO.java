package com.ktcloudinfra.ktx_reservation.domain.user.dto.request;

import lombok.Getter;

@Getter
public class LoginRequestDTO {
    private String username;
    private String password;
}

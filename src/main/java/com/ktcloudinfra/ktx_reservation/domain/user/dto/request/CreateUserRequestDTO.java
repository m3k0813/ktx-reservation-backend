package com.ktcloudinfra.ktx_reservation.domain.user.dto.request;

import lombok.Getter;

@Getter
public class CreateUserRequestDTO {
    private String username;
    private String password;
    private String name;
    private String email;
}

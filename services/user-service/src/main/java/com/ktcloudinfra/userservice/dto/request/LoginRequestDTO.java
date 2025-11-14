package com.ktcloudinfra.userservice.dto.request;

import lombok.Getter;

@Getter
public class LoginRequestDTO {
    private String username;
    private String password;
}

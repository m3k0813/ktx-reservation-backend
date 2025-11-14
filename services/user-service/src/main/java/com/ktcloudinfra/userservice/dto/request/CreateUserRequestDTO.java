package com.ktcloudinfra.userservice.dto.request;

import lombok.Getter;

@Getter
public class CreateUserRequestDTO {
    private String username;
    private String password;
    private String name;
    private String email;
}

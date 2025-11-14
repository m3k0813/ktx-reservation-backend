package com.ktcloudinfra.userservice.global.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}

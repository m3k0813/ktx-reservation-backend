package com.ktcloudinfra.seatservice.global.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}

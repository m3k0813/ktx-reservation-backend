package com.ktcloudinfra.ktx_reservation.global.exeception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}

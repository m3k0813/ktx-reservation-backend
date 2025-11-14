package com.ktcloudinfra.reservationservice.dto.request;

import lombok.Getter;

@Getter
public class ReservationRequestDTO {
    private Long trainId;
    private String seatNumber;
}

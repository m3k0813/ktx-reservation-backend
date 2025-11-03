package com.ktcloudinfra.ktx_reservation.domain.revervation.dto.request;

import lombok.Getter;

@Getter
public class ReservationRequestDTO {
    private Long trainId;
    private String seatNumber;
}
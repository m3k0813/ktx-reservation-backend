package com.ktcloudinfra.reservationservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponseDTO {
    private Long reservationId;
    private String trainName;
    private int price;
    private String departureStation;
    private String arrivalStation;
    private String seatNumber;
    private LocalDateTime reservedAt;
}

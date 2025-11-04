package com.ktcloudinfra.ktx_reservation.domain.seat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatResponseDTO {
    private Long id;
    private String seatNumber;
    private boolean reserved;
}

package com.ktcloudinfra.reservationservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestedEvent implements Serializable {
    private Long reservationId;
    private Long trainId;
    private String seatNumber;
    private LocalDateTime timestamp;
}

package com.ktcloudinfra.ktx_reservation.domain.revervation.dto.response;

import com.ktcloudinfra.ktx_reservation.domain.revervation.entity.Reservation;
import com.ktcloudinfra.ktx_reservation.domain.train.entity.Train;
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

    public static ReservationResponseDTO from(Reservation reservation) {
        return ReservationResponseDTO.builder()
                .reservationId(reservation.getId())
                .trainName(reservation.getTrain().getName())
                .price(reservation.getTrain().getPrice())
                .departureStation(reservation.getTrain().getDepartureStation())
                .arrivalStation(reservation.getTrain().getArrivalStation())
                .seatNumber(reservation.getSeat().getSeatNumber())
                .reservedAt(reservation.getReservedAt())
                .build();
    }
}
package com.ktcloudinfra.reservationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainDTO {
    private Long id;
    private String name;
    private int price;
    private String arrivalStation;
    private String departureStation;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
    private int availableSeats;
}

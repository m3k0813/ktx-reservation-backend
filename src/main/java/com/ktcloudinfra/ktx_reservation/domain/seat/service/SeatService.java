package com.ktcloudinfra.ktx_reservation.domain.seat.service;

import com.ktcloudinfra.ktx_reservation.domain.seat.dto.response.SeatResponseDTO;

import java.util.List;

public interface SeatService {
    List<SeatResponseDTO> getSeatsByTrain(Long trainId);
}

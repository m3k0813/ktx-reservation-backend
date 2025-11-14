package com.ktcloudinfra.reservationservice.service;

import com.ktcloudinfra.reservationservice.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.reservationservice.dto.response.ReservationResponseDTO;

import java.util.List;

public interface ReservationService {
    Long reserve(Long userId, ReservationRequestDTO request);

    List<ReservationResponseDTO> getReservationsByUser(Long userId);

    ReservationResponseDTO getReservation(Long reservationId);

    void cancelReservation(Long reservationId);
}

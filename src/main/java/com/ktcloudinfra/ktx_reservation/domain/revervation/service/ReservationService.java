package com.ktcloudinfra.ktx_reservation.domain.revervation.service;

import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.response.ReservationResponseDTO;

import java.util.List;

public interface ReservationService {
    Long reserve(Long userId, ReservationRequestDTO request);

    List<ReservationResponseDTO> getReservationsByUser(Long userId);

    ReservationResponseDTO getReservation(Long reservationId);

    void cancelReservation(Long reservationId);
}

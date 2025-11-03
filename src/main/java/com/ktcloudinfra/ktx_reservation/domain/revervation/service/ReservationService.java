package com.ktcloudinfra.ktx_reservation.domain.revervation.service;

import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.request.ReservationRequestDTO;

public interface ReservationService {
    Long reserve(Long userId, ReservationRequestDTO request);
}

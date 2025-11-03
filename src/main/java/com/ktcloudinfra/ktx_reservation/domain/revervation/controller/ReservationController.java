package com.ktcloudinfra.ktx_reservation.domain.revervation.controller;

import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.ktx_reservation.domain.revervation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Long> reserve(
            @RequestParam Long userId,
            @RequestBody ReservationRequestDTO request) {
        Long reservationId = reservationService.reserve(userId, request);
        return ResponseEntity.ok(reservationId);
    }
}
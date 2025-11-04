package com.ktcloudinfra.ktx_reservation.domain.revervation.controller;

import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.response.ReservationResponseDTO;
import com.ktcloudinfra.ktx_reservation.domain.revervation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> getReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.getReservation(reservationId));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getMyReservations(@RequestParam Long userId) {
        return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok("예매가 취소되었습니다.");
    }
}
package com.ktcloudinfra.reservationservice.controller;

import com.ktcloudinfra.reservationservice.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.reservationservice.dto.response.ReservationResponseDTO;
import com.ktcloudinfra.reservationservice.service.ReservationService;
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
    public ResponseEntity<java.util.Map<String, Object>> reserve(
            @RequestParam Long userId,
            @RequestBody ReservationRequestDTO request) {
        Long reservationId = reservationService.reserve(userId, request);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("reservationId", reservationId);
        response.put("message", "예약이 완료되었습니다.");
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
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
        return ResponseEntity.ok("예약이 취소되었습니다.");
    }
}

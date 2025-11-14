package com.ktcloudinfra.seatservice.controller;

import com.ktcloudinfra.seatservice.dto.response.SeatResponseDTO;
import com.ktcloudinfra.seatservice.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seats")
public class SeatController {

    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<List<SeatResponseDTO>> getSeatsByTrain(@RequestParam Long trainId) {
        return ResponseEntity.ok(seatService.getSeatsByTrain(trainId));
    }

    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<String> reserveSeat(@PathVariable Long seatId) {
        seatService.reserveSeat(seatId);
        return ResponseEntity.ok("좌석 예약 완료");
    }

    @PostMapping("/{seatId}/cancel")
    public ResponseEntity<String> cancelSeat(@PathVariable Long seatId) {
        seatService.cancelSeat(seatId);
        return ResponseEntity.ok("좌석 취소 완료");
    }
}

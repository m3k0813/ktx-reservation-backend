package com.ktcloudinfra.reservationservice.client;

import com.ktcloudinfra.reservationservice.client.dto.SeatDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "seat-service", url = "${seat-service.url}")
public interface SeatClient {

    @GetMapping("/api/v1/seats")
    List<SeatDTO> getSeatsByTrain(@RequestParam Long trainId);

    @PostMapping("/api/v1/seats/{seatId}/reserve")
    String reserveSeat(@PathVariable Long seatId);

    @PostMapping("/api/v1/seats/{seatId}/cancel")
    String cancelSeat(@PathVariable Long seatId);
}

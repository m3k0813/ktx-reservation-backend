package com.ktcloudinfra.trainservice.controller;

import com.ktcloudinfra.trainservice.entity.Train;
import com.ktcloudinfra.trainservice.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    @GetMapping
    public ResponseEntity<List<Train>> getAllTrains() {
        List<Train> trains = trainService.getAllTrains();
        return ResponseEntity.ok(trains);
    }

    @GetMapping("/{trainId}")
    public ResponseEntity<Train> getTrain(@PathVariable Long trainId) {
        return ResponseEntity.ok(trainService.getTrain(trainId));
    }

    @PutMapping("/{trainId}/seats")
    public ResponseEntity<String> updateAvailableSeats(
            @PathVariable Long trainId,
            @RequestParam int availableSeats) {
        trainService.updateAvailableSeats(trainId, availableSeats);
        return ResponseEntity.ok("좌석 수 업데이트 완료");
    }
}

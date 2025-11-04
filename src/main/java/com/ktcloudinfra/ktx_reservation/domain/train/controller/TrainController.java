package com.ktcloudinfra.ktx_reservation.domain.train.controller;

import com.ktcloudinfra.ktx_reservation.domain.train.entity.Train;
import com.ktcloudinfra.ktx_reservation.domain.train.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
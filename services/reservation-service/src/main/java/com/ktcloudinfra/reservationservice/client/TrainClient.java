package com.ktcloudinfra.reservationservice.client;

import com.ktcloudinfra.reservationservice.client.dto.TrainDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "train-service", url = "${train-service.url}")
public interface TrainClient {

    @GetMapping("/api/v1/trains/{trainId}")
    TrainDTO getTrain(@PathVariable Long trainId);

    @PutMapping("/api/v1/trains/{trainId}/seats")
    String updateAvailableSeats(@PathVariable Long trainId, @RequestParam int availableSeats);
}

package com.ktcloudinfra.seatservice.client;

import com.ktcloudinfra.seatservice.client.dto.TrainDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "train-service", url = "${train-service.url}")
public interface TrainClient {

    @GetMapping("/api/v1/trains/{trainId}")
    TrainDTO getTrain(@PathVariable Long trainId);
}

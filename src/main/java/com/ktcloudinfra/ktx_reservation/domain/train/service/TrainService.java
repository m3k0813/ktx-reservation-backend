package com.ktcloudinfra.ktx_reservation.domain.train.service;

import com.ktcloudinfra.ktx_reservation.domain.train.entity.Train;

import java.util.List;

public interface TrainService {
    List<Train> getAllTrains();
}

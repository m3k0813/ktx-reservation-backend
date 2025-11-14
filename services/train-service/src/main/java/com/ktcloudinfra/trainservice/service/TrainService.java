package com.ktcloudinfra.trainservice.service;

import com.ktcloudinfra.trainservice.entity.Train;

import java.util.List;

public interface TrainService {
    List<Train> getAllTrains();

    Train getTrain(Long trainId);

    void updateAvailableSeats(Long trainId, int availableSeats);

    // Event-based methods
    void decrementAvailableSeats(Long trainId);

    void incrementAvailableSeats(Long trainId);
}

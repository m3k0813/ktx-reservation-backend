package com.ktcloudinfra.ktx_reservation.domain.train.service;

import com.ktcloudinfra.ktx_reservation.domain.train.entity.Train;
import com.ktcloudinfra.ktx_reservation.domain.train.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;

    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }
}

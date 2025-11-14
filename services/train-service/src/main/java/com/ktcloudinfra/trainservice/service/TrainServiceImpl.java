package com.ktcloudinfra.trainservice.service;

import com.ktcloudinfra.trainservice.entity.Train;
import com.ktcloudinfra.trainservice.repository.TrainRepository;
import com.ktcloudinfra.trainservice.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Train getTrain(Long trainId) {
        return trainRepository.findById(trainId)
                .orElseThrow(() -> new ApiException("열차가 존재하지 않습니다."));
    }

    @Override
    @Transactional
    public void updateAvailableSeats(Long trainId, int availableSeats) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ApiException("열차가 존재하지 않습니다."));
        train.updateAvailableSeats(availableSeats);
    }

    @Override
    @Transactional
    public void decrementAvailableSeats(Long trainId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ApiException("열차가 존재하지 않습니다."));
        train.decrementAvailableSeats();
    }

    @Override
    @Transactional
    public void incrementAvailableSeats(Long trainId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ApiException("열차가 존재하지 않습니다."));
        train.incrementAvailableSeats();
    }
}

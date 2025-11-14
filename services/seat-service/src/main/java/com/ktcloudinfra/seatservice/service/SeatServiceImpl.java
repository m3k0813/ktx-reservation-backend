package com.ktcloudinfra.seatservice.service;

import com.ktcloudinfra.seatservice.client.TrainClient;
import com.ktcloudinfra.seatservice.client.dto.TrainDTO;
import com.ktcloudinfra.seatservice.dto.response.SeatResponseDTO;
import com.ktcloudinfra.seatservice.entity.Seat;
import com.ktcloudinfra.seatservice.repository.SeatRepository;
import com.ktcloudinfra.seatservice.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final TrainClient trainClient;

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponseDTO> getSeatsByTrain(Long trainId) {
        var seats = seatRepository.findByTrainId(trainId);
        if (seats.isEmpty()) {
            throw new ApiException("해당 열차의 좌석 정보가 존재하지 않습니다.");
        }

        TrainDTO train = trainClient.getTrain(trainId);

        return seats.stream()
                .map(seat -> SeatResponseDTO.builder()
                        .id(seat.getId())
                        .name(train.getName())
                        .seatNumber(seat.getSeatNumber())
                        .reserved(seat.isReserved())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void reserveSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ApiException("좌석이 존재하지 않습니다."));

        if (seat.isReserved()) {
            throw new ApiException("이미 예약된 좌석입니다.");
        }

        seat.reserve();
    }

    @Override
    @Transactional
    public void cancelSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ApiException("좌석이 존재하지 않습니다."));

        if (!seat.isReserved()) {
            throw new ApiException("예약되지 않은 좌석입니다.");
        }

        seat.cancel();
    }

    @Override
    @Transactional
    public void reserveSeat(Long trainId, String seatNumber) {
        Seat seat = seatRepository.findByTrainIdAndSeatNumber(trainId, seatNumber)
                .orElseThrow(() -> new ApiException("좌석이 존재하지 않습니다."));

        if (seat.isReserved()) {
            throw new ApiException("이미 예약된 좌석입니다.");
        }

        seat.reserve();
    }

    @Override
    @Transactional
    public void cancelSeat(Long trainId, String seatNumber) {
        Seat seat = seatRepository.findByTrainIdAndSeatNumber(trainId, seatNumber)
                .orElseThrow(() -> new ApiException("좌석이 존재하지 않습니다."));

        if (!seat.isReserved()) {
            throw new ApiException("예약되지 않은 좌석입니다.");
        }

        seat.cancel();
    }
}

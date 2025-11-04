package com.ktcloudinfra.ktx_reservation.domain.seat.service;

import com.ktcloudinfra.ktx_reservation.domain.seat.dto.response.SeatResponseDTO;
import com.ktcloudinfra.ktx_reservation.domain.seat.repository.SeatRepository;
import com.ktcloudinfra.ktx_reservation.global.exeception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponseDTO> getSeatsByTrain(Long trainId) {
        var seats = seatRepository.findByTrainId(trainId);
        if (seats.isEmpty()) {
            throw new ApiException("해당 열차의 좌석 정보가 존재하지 않습니다.");
        }

        return seats.stream()
                .map(seat -> SeatResponseDTO.builder()
                        .id(seat.getId())
                        .seatNumber(seat.getSeatNumber())
                        .reserved(seat.isReserved())
                        .build())
                .toList();
    }
}

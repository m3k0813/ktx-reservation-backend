package com.ktcloudinfra.ktx_reservation.domain.revervation.service;

import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.ktx_reservation.domain.revervation.entity.Reservation;
import com.ktcloudinfra.ktx_reservation.domain.revervation.repository.ReservationRepository;
import com.ktcloudinfra.ktx_reservation.domain.seat.repository.SeatRepository;
import com.ktcloudinfra.ktx_reservation.domain.train.repository.TrainRepository;
import com.ktcloudinfra.ktx_reservation.domain.user.repository.UserRepository;
import com.ktcloudinfra.ktx_reservation.global.exeception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService{

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public Long reserve(Long userId, ReservationRequestDTO request) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("유저가 존재하지 않습니다."));

        var train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new ApiException("열차가 존재하지 않습니다."));

        var seat = seatRepository.findByTrainIdAndSeatNumber(request.getTrainId(), request.getSeatNumber())
                .orElseThrow(() -> new ApiException("해당 좌석이 존재하지 않습니다."));

        if (seat.isReserved()) {
            throw new ApiException("이미 예약된 좌석입니다.");
        }

        seat.reserve();

        Reservation reservation = Reservation.builder()
                .user(user)
                .train(train)
                .seat(seat)
                .reservedAt(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);

        return reservation.getId();
    }
}

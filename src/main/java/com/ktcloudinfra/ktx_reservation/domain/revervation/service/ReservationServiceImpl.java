package com.ktcloudinfra.ktx_reservation.domain.revervation.service;

import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.ktx_reservation.domain.revervation.dto.response.ReservationResponseDTO;
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
import java.util.List;

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

        train.updateAvailableSeats(train.getAvailableSeats() - 1);

        Reservation reservation = Reservation.builder()
                .user(user)
                .train(train)
                .seat(seat)
                .reservedAt(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);

        return reservation.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByUser(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        if (reservations.isEmpty()) {
            throw new ApiException("예매 내역이 없습니다.");
        }
        return reservations.stream()
                .map(ReservationResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException("해당 예약 내역이 없습니다."));

        return ReservationResponseDTO.from(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException("해당 예매가 존재하지 않습니다."));

        var seat = reservation.getSeat();
        var train = reservation.getTrain();

        if (!seat.isReserved()) {
            throw new ApiException("이미 취소된 좌석입니다.");
        }

        seat.cancel();

        train.updateAvailableSeats(train.getAvailableSeats() + 1);

        reservationRepository.delete(reservation);
    }
}
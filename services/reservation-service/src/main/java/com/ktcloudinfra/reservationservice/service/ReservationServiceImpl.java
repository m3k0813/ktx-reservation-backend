package com.ktcloudinfra.reservationservice.service;

import com.ktcloudinfra.reservationservice.client.SeatClient;
import com.ktcloudinfra.reservationservice.client.TrainClient;
import com.ktcloudinfra.reservationservice.client.UserClient;
import com.ktcloudinfra.reservationservice.client.dto.SeatDTO;
import com.ktcloudinfra.reservationservice.client.dto.TrainDTO;
import com.ktcloudinfra.reservationservice.client.dto.UserDTO;
import com.ktcloudinfra.reservationservice.config.RabbitMQConfig;
import com.ktcloudinfra.reservationservice.dto.event.ReservationCancelledEvent;
import com.ktcloudinfra.reservationservice.dto.event.ReservationRequestedEvent;
import com.ktcloudinfra.reservationservice.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.reservationservice.dto.response.ReservationResponseDTO;
import com.ktcloudinfra.reservationservice.entity.Reservation;
import com.ktcloudinfra.reservationservice.entity.ReservationStatus;
import com.ktcloudinfra.reservationservice.repository.ReservationRepository;
import com.ktcloudinfra.reservationservice.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserClient userClient;
    private final TrainClient trainClient;
    private final SeatClient seatClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public Long reserve(Long userId, ReservationRequestDTO request) {
        // 1. 조회는 Feign 사용 (동기)
        UserDTO user = userClient.getUser(userId);
        if (user == null) {
            throw new ApiException("유저가 존재하지 않습니다.");
        }

        TrainDTO train = trainClient.getTrain(request.getTrainId());
        if (train == null) {
            throw new ApiException("열차가 존재하지 않습니다.");
        }

        List<SeatDTO> seats = seatClient.getSeatsByTrain(request.getTrainId());
        SeatDTO targetSeat = seats.stream()
                .filter(seat -> seat.getSeatNumber().equals(request.getSeatNumber()))
                .findFirst()
                .orElseThrow(() -> new ApiException("해당 좌석이 존재하지 않습니다."));

        if (targetSeat.isReserved()) {
            throw new ApiException("이미 예약된 좌석입니다.");
        }

        // 2. 예약 생성 (PENDING 상태)
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .trainId(request.getTrainId())
                .seatId(targetSeat.getId())
                .seatNumber(request.getSeatNumber())
                .status(ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now())
                .build();
        reservation = reservationRepository.save(reservation);

        // 3. 이벤트 발행 (비동기)
        ReservationRequestedEvent event = new ReservationRequestedEvent(
                reservation.getId(),
                request.getTrainId(),
                request.getSeatNumber(),
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RESERVATION_EXCHANGE,
                "reservation.requested",
                event
        );

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
                .map(reservation -> {
                    TrainDTO train = trainClient.getTrain(reservation.getTrainId());
                    List<SeatDTO> seats = seatClient.getSeatsByTrain(reservation.getTrainId());
                    SeatDTO seat = seats.stream()
                            .filter(s -> s.getId().equals(reservation.getSeatId()))
                            .findFirst()
                            .orElseThrow(() -> new ApiException("좌석 정보를 찾을 수 없습니다."));

                    return ReservationResponseDTO.builder()
                            .reservationId(reservation.getId())
                            .trainName(train.getName())
                            .price(train.getPrice())
                            .departureStation(train.getDepartureStation())
                            .arrivalStation(train.getArrivalStation())
                            .seatNumber(seat.getSeatNumber())
                            .reservedAt(reservation.getReservedAt())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException("예약이 존재하지 않습니다."));

        TrainDTO train = trainClient.getTrain(reservation.getTrainId());
        List<SeatDTO> seats = seatClient.getSeatsByTrain(reservation.getTrainId());
        SeatDTO seat = seats.stream()
                .filter(s -> s.getId().equals(reservation.getSeatId()))
                .findFirst()
                .orElseThrow(() -> new ApiException("좌석 정보를 찾을 수 없습니다."));

        return ReservationResponseDTO.builder()
                .reservationId(reservation.getId())
                .trainName(train.getName())
                .price(train.getPrice())
                .departureStation(train.getDepartureStation())
                .arrivalStation(train.getArrivalStation())
                .seatNumber(seat.getSeatNumber())
                .reservedAt(reservation.getReservedAt())
                .build();
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException("예약이 존재하지 않습니다."));

        // 이벤트 발행
        ReservationCancelledEvent event = new ReservationCancelledEvent(
                reservation.getId(),
                reservation.getTrainId(),
                reservation.getSeatNumber(),
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RESERVATION_EXCHANGE,
                "reservation.cancelled",
                event
        );

        // Delete reservation
        reservationRepository.delete(reservation);
    }
}

package com.ktcloudinfra.reservationservice.service;

import com.ktcloudinfra.reservationservice.client.SeatClient;
import com.ktcloudinfra.reservationservice.client.TrainClient;
import com.ktcloudinfra.reservationservice.client.UserClient;
import com.ktcloudinfra.reservationservice.client.dto.SeatDTO;
import com.ktcloudinfra.reservationservice.client.dto.TrainDTO;
import com.ktcloudinfra.reservationservice.client.dto.UserDTO;
import com.ktcloudinfra.reservationservice.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.reservationservice.dto.response.ReservationResponseDTO;
import com.ktcloudinfra.reservationservice.entity.Reservation;
import com.ktcloudinfra.reservationservice.entity.ReservationStatus;
import com.ktcloudinfra.reservationservice.global.exception.ApiException;
import com.ktcloudinfra.reservationservice.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private TrainClient trainClient;

    @Mock
    private SeatClient seatClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    @DisplayName("예약 생성 성공")
    void reserve_Success() {
        // Given
        Long userId = 1L;
        ReservationRequestDTO request = new ReservationRequestDTO();
        ReflectionTestUtils.setField(request, "trainId", 100L);
        ReflectionTestUtils.setField(request, "seatNumber", "1A");

        UserDTO userDTO = new UserDTO(userId, "testuser", "홍길동", "test@example.com");
        TrainDTO trainDTO = createTrainDTO(100L, "KTX-101");
        List<SeatDTO> seatDTOs = Arrays.asList(
                createSeatDTO(10L, "1A", 100L, false),
                createSeatDTO(11L, "1B", 100L, false)
        );

        when(userClient.getUser(userId)).thenReturn(userDTO);
        when(trainClient.getTrain(100L)).thenReturn(trainDTO);
        when(seatClient.getSeatsByTrain(100L)).thenReturn(seatDTOs);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation res = invocation.getArgument(0);
            ReflectionTestUtils.setField(res, "id", 1L);
            return res;
        });

        // When
        Long reservationId = reservationService.reserve(userId, request);

        // Then
        assertThat(reservationId).isEqualTo(1L);
        verify(userClient).getUser(userId);
        verify(trainClient).getTrain(100L);
        verify(seatClient).getSeatsByTrain(100L);
        verify(reservationRepository).save(any(Reservation.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - 사용자 없음")
    void reserve_Fail_UserNotFound() {
        // Given
        Long userId = 999L;
        ReservationRequestDTO request = new ReservationRequestDTO();
        ReflectionTestUtils.setField(request, "trainId", 100L);
        ReflectionTestUtils.setField(request, "seatNumber", "1A");

        when(userClient.getUser(userId)).thenThrow(new ApiException("사용자가 존재하지 않습니다."));

        // When & Then
        assertThatThrownBy(() -> reservationService.reserve(userId, request))
                .isInstanceOf(ApiException.class);

        verify(userClient).getUser(userId);
        verify(trainClient, never()).getTrain(anyLong());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자별 예약 목록 조회 성공")
    void getReservationsByUser_Success() {
        // Given
        Long userId = 1L;
        Reservation res1 = createReservation(1L, userId, 100L, 10L, "1A");
        Reservation res2 = createReservation(2L, userId, 101L, 11L, "2B");

        TrainDTO train1 = createTrainDTO(100L, "KTX-101");
        TrainDTO train2 = createTrainDTO(101L, "KTX-102");
        List<SeatDTO> seats1 = Arrays.asList(createSeatDTO(10L, "1A", 100L, true));
        List<SeatDTO> seats2 = Arrays.asList(createSeatDTO(11L, "2B", 101L, true));

        when(reservationRepository.findByUserId(userId)).thenReturn(Arrays.asList(res1, res2));
        when(trainClient.getTrain(100L)).thenReturn(train1);
        when(trainClient.getTrain(101L)).thenReturn(train2);
        when(seatClient.getSeatsByTrain(100L)).thenReturn(seats1);
        when(seatClient.getSeatsByTrain(101L)).thenReturn(seats2);

        // When
        List<ReservationResponseDTO> reservations = reservationService.getReservationsByUser(userId);

        // Then
        assertThat(reservations).hasSize(2);
        verify(reservationRepository).findByUserId(userId);
        verify(trainClient).getTrain(100L);
        verify(trainClient).getTrain(101L);
        verify(seatClient).getSeatsByTrain(100L);
        verify(seatClient).getSeatsByTrain(101L);
    }

    @Test
    @DisplayName("예약 조회 성공")
    void getReservation_Success() {
        // Given
        Long reservationId = 1L;
        Long userId = 1L;
        Reservation reservation = createReservation(reservationId, userId, 100L, 10L, "1A");

        TrainDTO trainDTO = createTrainDTO(100L, "KTX-101");
        List<SeatDTO> seatDTOs = Arrays.asList(createSeatDTO(10L, "1A", 100L, true));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(trainClient.getTrain(100L)).thenReturn(trainDTO);
        when(seatClient.getSeatsByTrain(100L)).thenReturn(seatDTOs);

        // When
        ReservationResponseDTO response = reservationService.getReservation(reservationId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo(reservationId);
        verify(reservationRepository).findById(reservationId);
        verify(trainClient).getTrain(100L);
        verify(seatClient).getSeatsByTrain(100L);
    }

    @Test
    @DisplayName("예약 조회 실패 - 존재하지 않음")
    void getReservation_Fail_NotFound() {
        // Given
        Long reservationId = 999L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.getReservation(reservationId))
                .isInstanceOf(ApiException.class)
                .hasMessage("예약이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservation_Success() {
        // Given
        Long reservationId = 1L;
        Reservation reservation = createReservation(reservationId, 1L, 100L, 10L, "1A");

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationRepository).delete(reservation);

        // When
        reservationService.cancelReservation(reservationId);

        // Then
        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).delete(reservation);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("예약 취소 실패 - 존재하지 않음")
    void cancelReservation_Fail_NotFound() {
        // Given
        Long reservationId = 999L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId))
                .isInstanceOf(ApiException.class)
                .hasMessage("예약이 존재하지 않습니다.");

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("예약 생성 실패 - 열차 없음")
    void reserve_Fail_TrainNotFound() {
        // Given
        Long userId = 1L;
        ReservationRequestDTO request = new ReservationRequestDTO();
        ReflectionTestUtils.setField(request, "trainId", 999L);
        ReflectionTestUtils.setField(request, "seatNumber", "1A");

        UserDTO userDTO = new UserDTO(userId, "testuser", "홍길동", "test@example.com");
        when(userClient.getUser(userId)).thenReturn(userDTO);
        when(trainClient.getTrain(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> reservationService.reserve(userId, request))
                .isInstanceOf(ApiException.class)
                .hasMessage("열차가 존재하지 않습니다.");

        verify(userClient).getUser(userId);
        verify(trainClient).getTrain(999L);
    }

    @Test
    @DisplayName("예약 생성 실패 - 좌석 없음")
    void reserve_Fail_SeatNotFound() {
        // Given
        Long userId = 1L;
        ReservationRequestDTO request = new ReservationRequestDTO();
        ReflectionTestUtils.setField(request, "trainId", 100L);
        ReflectionTestUtils.setField(request, "seatNumber", "99Z");

        UserDTO userDTO = new UserDTO(userId, "testuser", "홍길동", "test@example.com");
        TrainDTO trainDTO = createTrainDTO(100L, "KTX-101");
        List<SeatDTO> seatDTOs = Arrays.asList(
                createSeatDTO(10L, "1A", 100L, false),
                createSeatDTO(11L, "1B", 100L, false)
        );

        when(userClient.getUser(userId)).thenReturn(userDTO);
        when(trainClient.getTrain(100L)).thenReturn(trainDTO);
        when(seatClient.getSeatsByTrain(100L)).thenReturn(seatDTOs);

        // When & Then
        assertThatThrownBy(() -> reservationService.reserve(userId, request))
                .isInstanceOf(ApiException.class)
                .hasMessage("해당 좌석이 존재하지 않습니다.");

        verify(seatClient).getSeatsByTrain(100L);
    }

    @Test
    @DisplayName("예약 생성 실패 - 이미 예약된 좌석")
    void reserve_Fail_SeatAlreadyReserved() {
        // Given
        Long userId = 1L;
        ReservationRequestDTO request = new ReservationRequestDTO();
        ReflectionTestUtils.setField(request, "trainId", 100L);
        ReflectionTestUtils.setField(request, "seatNumber", "1A");

        UserDTO userDTO = new UserDTO(userId, "testuser", "홍길동", "test@example.com");
        TrainDTO trainDTO = createTrainDTO(100L, "KTX-101");
        List<SeatDTO> seatDTOs = Arrays.asList(
                createSeatDTO(10L, "1A", 100L, true), // Already reserved
                createSeatDTO(11L, "1B", 100L, false)
        );

        when(userClient.getUser(userId)).thenReturn(userDTO);
        when(trainClient.getTrain(100L)).thenReturn(trainDTO);
        when(seatClient.getSeatsByTrain(100L)).thenReturn(seatDTOs);

        // When & Then
        assertThatThrownBy(() -> reservationService.reserve(userId, request))
                .isInstanceOf(ApiException.class)
                .hasMessage("이미 예약된 좌석입니다.");

        verify(seatClient).getSeatsByTrain(100L);
    }

    @Test
    @DisplayName("사용자별 예약 목록 조회 실패 - 내역 없음")
    void getReservationsByUser_Fail_NoReservations() {
        // Given
        Long userId = 1L;
        when(reservationRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> reservationService.getReservationsByUser(userId))
                .isInstanceOf(ApiException.class)
                .hasMessage("예매 내역이 없습니다.");

        verify(reservationRepository).findByUserId(userId);
    }

    private Reservation createReservation(Long id, Long userId, Long trainId, Long seatId, String seatNumber) {
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .trainId(trainId)
                .seatId(seatId)
                .seatNumber(seatNumber)
                .status(ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    private TrainDTO createTrainDTO(Long id, String name) {
        return TrainDTO.builder()
                .id(id)
                .name(name)
                .price(50000)
                .departureStation("서울")
                .arrivalStation("부산")
                .departureTime(LocalDateTime.now())
                .arrivalTime(LocalDateTime.now().plusHours(3))
                .availableSeats(100)
                .build();
    }

    private SeatDTO createSeatDTO(Long id, String seatNumber, Long trainId, boolean reserved) {
        return SeatDTO.builder()
                .id(id)
                .name("KTX-101")
                .seatNumber(seatNumber)
                .reserved(reserved)
                .build();
    }
}

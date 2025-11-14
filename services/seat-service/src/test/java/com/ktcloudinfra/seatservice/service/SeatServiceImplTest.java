package com.ktcloudinfra.seatservice.service;

import com.ktcloudinfra.seatservice.client.TrainClient;
import com.ktcloudinfra.seatservice.client.dto.TrainDTO;
import com.ktcloudinfra.seatservice.dto.response.SeatResponseDTO;
import com.ktcloudinfra.seatservice.entity.Seat;
import com.ktcloudinfra.seatservice.global.exception.ApiException;
import com.ktcloudinfra.seatservice.repository.SeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TrainClient trainClient;

    @InjectMocks
    private SeatServiceImpl seatService;

    @Test
    @DisplayName("열차 ID로 좌석 목록 조회 성공")
    void getSeatsByTrain_Success() {
        // Given
        Long trainId = 100L;
        Seat seat1 = createSeat(1L, "1A", trainId, false);
        Seat seat2 = createSeat(2L, "1B", trainId, true);

        TrainDTO trainDTO = TrainDTO.builder()
                .id(trainId)
                .name("KTX-101")
                .price(50000)
                .departureStation("서울")
                .arrivalStation("부산")
                .departureTime(LocalDateTime.now())
                .arrivalTime(LocalDateTime.now().plusHours(3))
                .availableSeats(98)
                .build();

        when(seatRepository.findByTrainId(trainId)).thenReturn(Arrays.asList(seat1, seat2));
        when(trainClient.getTrain(trainId)).thenReturn(trainDTO);

        // When
        List<SeatResponseDTO> seats = seatService.getSeatsByTrain(trainId);

        // Then
        assertThat(seats).hasSize(2);
        assertThat(seats.get(0).getName()).isEqualTo("KTX-101");
        assertThat(seats.get(0).getSeatNumber()).isEqualTo("1A");
        assertThat(seats.get(0).isReserved()).isFalse();
        assertThat(seats.get(1).getSeatNumber()).isEqualTo("1B");
        assertThat(seats.get(1).isReserved()).isTrue();

        verify(seatRepository).findByTrainId(trainId);
        verify(trainClient).getTrain(trainId);
    }

    @Test
    @DisplayName("열차 ID로 좌석 목록 조회 실패 - 좌석 없음")
    void getSeatsByTrain_Fail_NoSeats() {
        // Given
        Long trainId = 999L;
        when(seatRepository.findByTrainId(trainId)).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> seatService.getSeatsByTrain(trainId))
                .isInstanceOf(ApiException.class)
                .hasMessage("해당 열차의 좌석 정보가 존재하지 않습니다.");

        verify(seatRepository).findByTrainId(trainId);
        verify(trainClient, never()).getTrain(anyLong());
    }

    @Test
    @DisplayName("좌석 ID로 예약 성공")
    void reserveSeat_ById_Success() {
        // Given
        Long seatId = 1L;
        Seat seat = createSeat(seatId, "1A", 100L, false);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // When
        seatService.reserveSeat(seatId);

        // Then
        assertThat(seat.isReserved()).isTrue();
        verify(seatRepository).findById(seatId);
    }

    @Test
    @DisplayName("좌석 ID로 예약 실패 - 좌석 없음")
    void reserveSeat_ById_Fail_SeatNotFound() {
        // Given
        Long seatId = 999L;
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seatService.reserveSeat(seatId))
                .isInstanceOf(ApiException.class)
                .hasMessage("좌석이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("좌석 ID로 예약 실패 - 이미 예약됨")
    void reserveSeat_ById_Fail_AlreadyReserved() {
        // Given
        Long seatId = 1L;
        Seat seat = createSeat(seatId, "1A", 100L, true);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // When & Then
        assertThatThrownBy(() -> seatService.reserveSeat(seatId))
                .isInstanceOf(ApiException.class)
                .hasMessage("이미 예약된 좌석입니다.");
    }

    @Test
    @DisplayName("좌석 ID로 취소 성공")
    void cancelSeat_ById_Success() {
        // Given
        Long seatId = 1L;
        Seat seat = createSeat(seatId, "1A", 100L, true);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // When
        seatService.cancelSeat(seatId);

        // Then
        assertThat(seat.isReserved()).isFalse();
        verify(seatRepository).findById(seatId);
    }

    @Test
    @DisplayName("좌석 ID로 취소 실패 - 좌석 없음")
    void cancelSeat_ById_Fail_SeatNotFound() {
        // Given
        Long seatId = 999L;
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seatService.cancelSeat(seatId))
                .isInstanceOf(ApiException.class)
                .hasMessage("좌석이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("좌석 ID로 취소 실패 - 예약되지 않음")
    void cancelSeat_ById_Fail_NotReserved() {
        // Given
        Long seatId = 1L;
        Seat seat = createSeat(seatId, "1A", 100L, false);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // When & Then
        assertThatThrownBy(() -> seatService.cancelSeat(seatId))
                .isInstanceOf(ApiException.class)
                .hasMessage("예약되지 않은 좌석입니다.");
    }

    @Test
    @DisplayName("열차 ID와 좌석번호로 예약 성공")
    void reserveSeat_ByTrainIdAndSeatNumber_Success() {
        // Given
        Long trainId = 100L;
        String seatNumber = "1A";
        Seat seat = createSeat(1L, seatNumber, trainId, false);

        when(seatRepository.findByTrainIdAndSeatNumber(trainId, seatNumber))
                .thenReturn(Optional.of(seat));

        // When
        seatService.reserveSeat(trainId, seatNumber);

        // Then
        assertThat(seat.isReserved()).isTrue();
        verify(seatRepository).findByTrainIdAndSeatNumber(trainId, seatNumber);
    }

    @Test
    @DisplayName("열차 ID와 좌석번호로 예약 실패 - 좌석 없음")
    void reserveSeat_ByTrainIdAndSeatNumber_Fail_SeatNotFound() {
        // Given
        Long trainId = 100L;
        String seatNumber = "1A";

        when(seatRepository.findByTrainIdAndSeatNumber(trainId, seatNumber))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seatService.reserveSeat(trainId, seatNumber))
                .isInstanceOf(ApiException.class)
                .hasMessage("좌석이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("열차 ID와 좌석번호로 예약 실패 - 이미 예약됨")
    void reserveSeat_ByTrainIdAndSeatNumber_Fail_AlreadyReserved() {
        // Given
        Long trainId = 100L;
        String seatNumber = "1A";
        Seat seat = createSeat(1L, seatNumber, trainId, true);

        when(seatRepository.findByTrainIdAndSeatNumber(trainId, seatNumber))
                .thenReturn(Optional.of(seat));

        // When & Then
        assertThatThrownBy(() -> seatService.reserveSeat(trainId, seatNumber))
                .isInstanceOf(ApiException.class)
                .hasMessage("이미 예약된 좌석입니다.");
    }

    @Test
    @DisplayName("열차 ID와 좌석번호로 취소 성공")
    void cancelSeat_ByTrainIdAndSeatNumber_Success() {
        // Given
        Long trainId = 100L;
        String seatNumber = "1A";
        Seat seat = createSeat(1L, seatNumber, trainId, true);

        when(seatRepository.findByTrainIdAndSeatNumber(trainId, seatNumber))
                .thenReturn(Optional.of(seat));

        // When
        seatService.cancelSeat(trainId, seatNumber);

        // Then
        assertThat(seat.isReserved()).isFalse();
        verify(seatRepository).findByTrainIdAndSeatNumber(trainId, seatNumber);
    }

    @Test
    @DisplayName("열차 ID와 좌석번호로 취소 실패 - 좌석 없음")
    void cancelSeat_ByTrainIdAndSeatNumber_Fail_SeatNotFound() {
        // Given
        Long trainId = 100L;
        String seatNumber = "1A";

        when(seatRepository.findByTrainIdAndSeatNumber(trainId, seatNumber))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seatService.cancelSeat(trainId, seatNumber))
                .isInstanceOf(ApiException.class)
                .hasMessage("좌석이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("열차 ID와 좌석번호로 취소 실패 - 예약되지 않음")
    void cancelSeat_ByTrainIdAndSeatNumber_Fail_NotReserved() {
        // Given
        Long trainId = 100L;
        String seatNumber = "1A";
        Seat seat = createSeat(1L, seatNumber, trainId, false);

        when(seatRepository.findByTrainIdAndSeatNumber(trainId, seatNumber))
                .thenReturn(Optional.of(seat));

        // When & Then
        assertThatThrownBy(() -> seatService.cancelSeat(trainId, seatNumber))
                .isInstanceOf(ApiException.class)
                .hasMessage("예약되지 않은 좌석입니다.");
    }

    private Seat createSeat(Long id, String seatNumber, Long trainId, boolean reserved) {
        Seat seat = Seat.builder()
                .seatNumber(seatNumber)
                .trainId(trainId)
                .build();
        ReflectionTestUtils.setField(seat, "id", id);
        if (reserved) {
            seat.reserve();
        }
        return seat;
    }
}

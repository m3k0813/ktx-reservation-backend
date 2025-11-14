package com.ktcloudinfra.trainservice.service;

import com.ktcloudinfra.trainservice.entity.Train;
import com.ktcloudinfra.trainservice.global.exception.ApiException;
import com.ktcloudinfra.trainservice.repository.TrainRepository;
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
class TrainServiceImplTest {

    @Mock
    private TrainRepository trainRepository;

    @InjectMocks
    private TrainServiceImpl trainService;

    @Test
    @DisplayName("모든 열차 조회 성공")
    void getAllTrains_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Train train1 = createTrain(1L, "KTX-101", 50000, 100, now);
        Train train2 = createTrain(2L, "KTX-102", 45000, 80, now);

        when(trainRepository.findAll()).thenReturn(Arrays.asList(train1, train2));

        // When
        List<Train> trains = trainService.getAllTrains();

        // Then
        assertThat(trains).hasSize(2);
        assertThat(trains.get(0).getName()).isEqualTo("KTX-101");
        assertThat(trains.get(1).getName()).isEqualTo("KTX-102");
        verify(trainRepository).findAll();
    }

    @Test
    @DisplayName("열차 조회 성공")
    void getTrain_Success() {
        // Given
        Long trainId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Train train = createTrain(trainId, "KTX-101", 50000, 100, now);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(train));

        // When
        Train foundTrain = trainService.getTrain(trainId);

        // Then
        assertThat(foundTrain).isNotNull();
        assertThat(foundTrain.getId()).isEqualTo(trainId);
        assertThat(foundTrain.getName()).isEqualTo("KTX-101");
        verify(trainRepository).findById(trainId);
    }

    @Test
    @DisplayName("열차 조회 실패 - 존재하지 않음")
    void getTrain_Fail_NotFound() {
        // Given
        Long trainId = 999L;
        when(trainRepository.findById(trainId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> trainService.getTrain(trainId))
                .isInstanceOf(ApiException.class)
                .hasMessage("열차가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("가용 좌석 수 업데이트 성공")
    void updateAvailableSeats_Success() {
        // Given
        Long trainId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Train train = createTrain(trainId, "KTX-101", 50000, 100, now);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(train));

        // When
        trainService.updateAvailableSeats(trainId, 80);

        // Then
        assertThat(train.getAvailableSeats()).isEqualTo(80);
        verify(trainRepository).findById(trainId);
    }

    @Test
    @DisplayName("가용 좌석 수 업데이트 실패 - 열차 없음")
    void updateAvailableSeats_Fail_TrainNotFound() {
        // Given
        Long trainId = 999L;
        when(trainRepository.findById(trainId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> trainService.updateAvailableSeats(trainId, 80))
                .isInstanceOf(ApiException.class)
                .hasMessage("열차가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("가용 좌석 수 감소 성공")
    void decrementAvailableSeats_Success() {
        // Given
        Long trainId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Train train = createTrain(trainId, "KTX-101", 50000, 100, now);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(train));

        // When
        trainService.decrementAvailableSeats(trainId);

        // Then
        assertThat(train.getAvailableSeats()).isEqualTo(99);
        verify(trainRepository).findById(trainId);
    }

    @Test
    @DisplayName("가용 좌석 수 감소 실패 - 열차 없음")
    void decrementAvailableSeats_Fail_TrainNotFound() {
        // Given
        Long trainId = 999L;
        when(trainRepository.findById(trainId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> trainService.decrementAvailableSeats(trainId))
                .isInstanceOf(ApiException.class)
                .hasMessage("열차가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("가용 좌석 수 증가 성공")
    void incrementAvailableSeats_Success() {
        // Given
        Long trainId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Train train = createTrain(trainId, "KTX-101", 50000, 50, now);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(train));

        // When
        trainService.incrementAvailableSeats(trainId);

        // Then
        assertThat(train.getAvailableSeats()).isEqualTo(51);
        verify(trainRepository).findById(trainId);
    }

    @Test
    @DisplayName("가용 좌석 수 증가 실패 - 열차 없음")
    void incrementAvailableSeats_Fail_TrainNotFound() {
        // Given
        Long trainId = 999L;
        when(trainRepository.findById(trainId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> trainService.incrementAvailableSeats(trainId))
                .isInstanceOf(ApiException.class)
                .hasMessage("열차가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("여러 번 좌석 증감 테스트")
    void multipleIncrementAndDecrement() {
        // Given
        Long trainId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Train train = createTrain(trainId, "KTX-101", 50000, 100, now);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(train));

        // When - 2번 감소
        trainService.decrementAvailableSeats(trainId);
        trainService.decrementAvailableSeats(trainId);

        // Then
        assertThat(train.getAvailableSeats()).isEqualTo(98);

        // When - 1번 증가
        trainService.incrementAvailableSeats(trainId);

        // Then
        assertThat(train.getAvailableSeats()).isEqualTo(99);
    }

    private Train createTrain(Long id, String name, int price, int availableSeats, LocalDateTime time) {
        Train train = Train.builder()
                .name(name)
                .price(price)
                .departureStation("서울")
                .arrivalStation("부산")
                .departureTime(time)
                .arrivalTime(time.plusHours(3))
                .availableSeats(availableSeats)
                .build();
        ReflectionTestUtils.setField(train, "id", id);
        return train;
    }
}

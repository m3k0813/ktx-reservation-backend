package com.ktcloudinfra.trainservice.controller;

import com.ktcloudinfra.trainservice.entity.Train;
import com.ktcloudinfra.trainservice.global.exception.ApiException;
import com.ktcloudinfra.trainservice.service.TrainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainController.class)
@Import(com.ktcloudinfra.trainservice.global.config.SecurityConfig.class)
class TrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainService trainService;

    @Test
    @DisplayName("GET /api/v1/trains - 모든 열차 조회 성공")
    void getAllTrains_Success() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Train train1 = createTrain(1L, "KTX-101", 50000, 100, now);
        Train train2 = createTrain(2L, "KTX-102", 45000, 80, now);
        List<Train> trains = Arrays.asList(train1, train2);

        when(trainService.getAllTrains()).thenReturn(trains);

        // When & Then
        mockMvc.perform(get("/api/v1/trains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("KTX-101"))
                .andExpect(jsonPath("$[0].price").value(50000))
                .andExpect(jsonPath("$[0].availableSeats").value(100))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("KTX-102"));

        verify(trainService).getAllTrains();
    }

    @Test
    @DisplayName("GET /api/v1/trains - 빈 목록 반환")
    void getAllTrains_EmptyList() throws Exception {
        // Given
        when(trainService.getAllTrains()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/trains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(trainService).getAllTrains();
    }

    @Test
    @DisplayName("GET /api/v1/trains/{trainId} - 열차 조회 성공")
    void getTrain_Success() throws Exception {
        // Given
        Long trainId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Train train = createTrain(trainId, "KTX-101", 50000, 100, now);

        when(trainService.getTrain(trainId)).thenReturn(train);

        // When & Then
        mockMvc.perform(get("/api/v1/trains/{trainId}", trainId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("KTX-101"))
                .andExpect(jsonPath("$.price").value(50000))
                .andExpect(jsonPath("$.departureStation").value("서울"))
                .andExpect(jsonPath("$.arrivalStation").value("부산"))
                .andExpect(jsonPath("$.availableSeats").value(100));

        verify(trainService).getTrain(trainId);
    }

    @Test
    @DisplayName("GET /api/v1/trains/{trainId} - 열차 조회 실패 (존재하지 않음)")
    void getTrain_Fail_NotFound() throws Exception {
        // Given
        Long trainId = 999L;
        when(trainService.getTrain(trainId))
                .thenThrow(new ApiException("열차가 존재하지 않습니다."));

        // When & Then
        mockMvc.perform(get("/api/v1/trains/{trainId}", trainId))
                .andExpect(status().isBadRequest());

        verify(trainService).getTrain(trainId);
    }

    @Test
    @DisplayName("PUT /api/v1/trains/{trainId}/seats - 좌석 수 업데이트 성공")
    void updateAvailableSeats_Success() throws Exception {
        // Given
        Long trainId = 1L;
        int newSeats = 80;

        doNothing().when(trainService).updateAvailableSeats(trainId, newSeats);

        // When & Then
        mockMvc.perform(put("/api/v1/trains/{trainId}/seats", trainId)
                        .param("availableSeats", String.valueOf(newSeats)))
                .andExpect(status().isOk())
                .andExpect(content().string("좌석 수 업데이트 완료"));

        verify(trainService).updateAvailableSeats(trainId, newSeats);
    }

    @Test
    @DisplayName("PUT /api/v1/trains/{trainId}/seats - 좌석 수 업데이트 실패 (열차 없음)")
    void updateAvailableSeats_Fail_TrainNotFound() throws Exception {
        // Given
        Long trainId = 999L;
        int newSeats = 80;

        doThrow(new ApiException("열차가 존재하지 않습니다."))
                .when(trainService).updateAvailableSeats(trainId, newSeats);

        // When & Then
        mockMvc.perform(put("/api/v1/trains/{trainId}/seats", trainId)
                        .param("availableSeats", String.valueOf(newSeats)))
                .andExpect(status().isBadRequest());

        verify(trainService).updateAvailableSeats(trainId, newSeats);
    }

    @Test
    @DisplayName("PUT /api/v1/trains/{trainId}/seats - 0으로 업데이트")
    void updateAvailableSeats_ToZero() throws Exception {
        // Given
        Long trainId = 1L;
        int newSeats = 0;

        doNothing().when(trainService).updateAvailableSeats(trainId, newSeats);

        // When & Then
        mockMvc.perform(put("/api/v1/trains/{trainId}/seats", trainId)
                        .param("availableSeats", String.valueOf(newSeats)))
                .andExpect(status().isOk())
                .andExpect(content().string("좌석 수 업데이트 완료"));

        verify(trainService).updateAvailableSeats(trainId, newSeats);
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

package com.ktcloudinfra.seatservice.controller;

import com.ktcloudinfra.seatservice.dto.response.SeatResponseDTO;
import com.ktcloudinfra.seatservice.global.exception.ApiException;
import com.ktcloudinfra.seatservice.service.SeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
@Import(com.ktcloudinfra.seatservice.global.config.SecurityConfig.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatService seatService;

    @Test
    @DisplayName("GET /api/v1/seats?trainId=100 - 좌석 목록 조회 성공")
    void getSeatsByTrain_Success() throws Exception {
        // Given
        Long trainId = 100L;
        List<SeatResponseDTO> seats = Arrays.asList(
                SeatResponseDTO.builder().id(1L).name("KTX-101").seatNumber("1A").reserved(false).build(),
                SeatResponseDTO.builder().id(2L).name("KTX-101").seatNumber("1B").reserved(true).build()
        );

        when(seatService.getSeatsByTrain(trainId)).thenReturn(seats);

        // When & Then
        mockMvc.perform(get("/api/v1/seats")
                        .param("trainId", trainId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].seatNumber").value("1A"))
                .andExpect(jsonPath("$[0].reserved").value(false))
                .andExpect(jsonPath("$[1].seatNumber").value("1B"))
                .andExpect(jsonPath("$[1].reserved").value(true));

        verify(seatService).getSeatsByTrain(trainId);
    }

    @Test
    @DisplayName("GET /api/v1/seats?trainId=999 - 좌석 목록 조회 실패")
    void getSeatsByTrain_Fail_NoSeats() throws Exception {
        // Given
        Long trainId = 999L;
        when(seatService.getSeatsByTrain(trainId))
                .thenThrow(new ApiException("해당 열차의 좌석 정보가 존재하지 않습니다."));

        // When & Then
        mockMvc.perform(get("/api/v1/seats")
                        .param("trainId", trainId.toString()))
                .andExpect(status().isBadRequest());

        verify(seatService).getSeatsByTrain(trainId);
    }

    @Test
    @DisplayName("POST /api/v1/seats/{seatId}/reserve - 좌석 예약 성공")
    void reserveSeat_Success() throws Exception {
        // Given
        Long seatId = 1L;
        doNothing().when(seatService).reserveSeat(seatId);

        // When & Then
        mockMvc.perform(post("/api/v1/seats/{seatId}/reserve", seatId))
                .andExpect(status().isOk())
                .andExpect(content().string("좌석 예약 완료"));

        verify(seatService).reserveSeat(seatId);
    }

    @Test
    @DisplayName("POST /api/v1/seats/{seatId}/reserve - 좌석 예약 실패 (이미 예약됨)")
    void reserveSeat_Fail_AlreadyReserved() throws Exception {
        // Given
        Long seatId = 1L;
        doThrow(new ApiException("이미 예약된 좌석입니다."))
                .when(seatService).reserveSeat(seatId);

        // When & Then
        mockMvc.perform(post("/api/v1/seats/{seatId}/reserve", seatId))
                .andExpect(status().isBadRequest());

        verify(seatService).reserveSeat(seatId);
    }

    @Test
    @DisplayName("POST /api/v1/seats/{seatId}/cancel - 좌석 취소 성공")
    void cancelSeat_Success() throws Exception {
        // Given
        Long seatId = 1L;
        doNothing().when(seatService).cancelSeat(seatId);

        // When & Then
        mockMvc.perform(post("/api/v1/seats/{seatId}/cancel", seatId))
                .andExpect(status().isOk())
                .andExpect(content().string("좌석 취소 완료"));

        verify(seatService).cancelSeat(seatId);
    }

    @Test
    @DisplayName("POST /api/v1/seats/{seatId}/cancel - 좌석 취소 실패 (예약되지 않음)")
    void cancelSeat_Fail_NotReserved() throws Exception {
        // Given
        Long seatId = 1L;
        doThrow(new ApiException("예약되지 않은 좌석입니다."))
                .when(seatService).cancelSeat(seatId);

        // When & Then
        mockMvc.perform(post("/api/v1/seats/{seatId}/cancel", seatId))
                .andExpect(status().isBadRequest());

        verify(seatService).cancelSeat(seatId);
    }
}

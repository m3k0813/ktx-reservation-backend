package com.ktcloudinfra.reservationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktcloudinfra.reservationservice.dto.request.ReservationRequestDTO;
import com.ktcloudinfra.reservationservice.dto.response.ReservationResponseDTO;
import com.ktcloudinfra.reservationservice.entity.ReservationStatus;
import com.ktcloudinfra.reservationservice.global.exception.ApiException;
import com.ktcloudinfra.reservationservice.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import(com.ktcloudinfra.reservationservice.global.config.SecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @Test
    @DisplayName("POST /api/v1/reservations?userId=1 - 예약 생성 성공")
    void createReservation_Success() throws Exception {
        // Given
        Long userId = 1L;
        ReservationRequestDTO request = new ReservationRequestDTO();
        ReflectionTestUtils.setField(request, "trainId", 100L);
        ReflectionTestUtils.setField(request, "seatNumber", "1A");

        when(reservationService.reserve(anyLong(), any(ReservationRequestDTO.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.message").value("예약이 완료되었습니다."));

        verify(reservationService).reserve(anyLong(), any(ReservationRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/reservations - 예약 생성 실패 (사용자 없음)")
    void createReservation_Fail_UserNotFound() throws Exception {
        // Given
        Long userId = 999L;
        ReservationRequestDTO request = new ReservationRequestDTO();
        ReflectionTestUtils.setField(request, "trainId", 100L);
        ReflectionTestUtils.setField(request, "seatNumber", "1A");

        when(reservationService.reserve(anyLong(), any(ReservationRequestDTO.class)))
                .thenThrow(new ApiException("사용자가 존재하지 않습니다."));

        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reservationService).reserve(anyLong(), any(ReservationRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/reservations?userId=1 - 사용자 예약 목록 조회")
    void getReservationsByUser_Success() throws Exception {
        // Given
        Long userId = 1L;
        List<ReservationResponseDTO> reservations = Arrays.asList(
                createReservationResponse(1L, "KTX-101", "1A", "서울", "부산", 50000),
                createReservationResponse(2L, "KTX-102", "2B", "서울", "대전", 30000)
        );

        when(reservationService.getReservationsByUser(userId)).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/v1/reservations")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].trainName").value("KTX-101"))
                .andExpect(jsonPath("$[1].reservationId").value(2));

        verify(reservationService).getReservationsByUser(userId);
    }

    @Test
    @DisplayName("GET /api/v1/reservations/{reservationId} - 예약 조회 성공")
    void getReservation_Success() throws Exception {
        // Given
        Long reservationId = 1L;
        ReservationResponseDTO response = createReservationResponse(
                reservationId, "KTX-101", "1A", "서울", "부산", 50000
        );

        when(reservationService.getReservation(reservationId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/reservations/{reservationId}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.trainName").value("KTX-101"))
                .andExpect(jsonPath("$.seatNumber").value("1A"))
                .andExpect(jsonPath("$.price").value(50000));

        verify(reservationService).getReservation(reservationId);
    }

    @Test
    @DisplayName("GET /api/v1/reservations/{reservationId} - 예약 조회 실패")
    void getReservation_Fail_NotFound() throws Exception {
        // Given
        Long reservationId = 999L;
        when(reservationService.getReservation(reservationId))
                .thenThrow(new ApiException("예약이 존재하지 않습니다."));

        // When & Then
        mockMvc.perform(get("/api/v1/reservations/{reservationId}", reservationId))
                .andExpect(status().isBadRequest());

        verify(reservationService).getReservation(reservationId);
    }

    @Test
    @DisplayName("DELETE /api/v1/reservations/{reservationId} - 예약 취소 성공")
    void cancelReservation_Success() throws Exception {
        // Given
        Long reservationId = 1L;
        doNothing().when(reservationService).cancelReservation(reservationId);

        // When & Then
        mockMvc.perform(delete("/api/v1/reservations/{reservationId}", reservationId))
                .andExpect(status().isOk())
                .andExpect(content().string("예약이 취소되었습니다."));

        verify(reservationService).cancelReservation(reservationId);
    }

    @Test
    @DisplayName("DELETE /api/v1/reservations/{reservationId} - 예약 취소 실패")
    void cancelReservation_Fail_NotFound() throws Exception {
        // Given
        Long reservationId = 999L;
        doThrow(new ApiException("예약이 존재하지 않습니다."))
                .when(reservationService).cancelReservation(reservationId);

        // When & Then
        mockMvc.perform(delete("/api/v1/reservations/{reservationId}", reservationId))
                .andExpect(status().isBadRequest());

        verify(reservationService).cancelReservation(reservationId);
    }

    private ReservationResponseDTO createReservationResponse(
            Long reservationId, String trainName, String seatNumber,
            String departureStation, String arrivalStation, int price) {
        return ReservationResponseDTO.builder()
                .reservationId(reservationId)
                .trainName(trainName)
                .seatNumber(seatNumber)
                .departureStation(departureStation)
                .arrivalStation(arrivalStation)
                .price(price)
                .reservedAt(LocalDateTime.now())
                .build();
    }
}

package com.ktcloudinfra.seatservice.listener;

import com.ktcloudinfra.seatservice.dto.event.ReservationCancelledEvent;
import com.ktcloudinfra.seatservice.dto.event.ReservationRequestedEvent;
import com.ktcloudinfra.seatservice.service.SeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatEventListenerTest {

    @Mock
    private SeatService seatService;

    @InjectMocks
    private SeatEventListener seatEventListener;

    @Test
    @DisplayName("예약 요청 이벤트 처리 성공")
    void handleReservationRequested_Success() {
        // Given
        ReservationRequestedEvent event = new ReservationRequestedEvent(
                1L, 100L, "1A", LocalDateTime.now()
        );

        doNothing().when(seatService).reserveSeat(100L, "1A");

        // When
        seatEventListener.handleReservationRequested(event);

        // Then
        verify(seatService).reserveSeat(100L, "1A");
    }

    @Test
    @DisplayName("예약 요청 이벤트 처리 실패 - 예외 처리")
    void handleReservationRequested_Fail_ExceptionHandled() {
        // Given
        ReservationRequestedEvent event = new ReservationRequestedEvent(
                1L, 100L, "1A", LocalDateTime.now()
        );

        doThrow(new RuntimeException("좌석이 존재하지 않습니다."))
                .when(seatService).reserveSeat(100L, "1A");

        // When - 예외가 발생해도 로깅만 하고 계속 진행
        seatEventListener.handleReservationRequested(event);

        // Then
        verify(seatService).reserveSeat(100L, "1A");
    }

    @Test
    @DisplayName("예약 취소 이벤트 처리 성공")
    void handleReservationCancelled_Success() {
        // Given
        ReservationCancelledEvent event = new ReservationCancelledEvent(
                1L, 100L, "1A", LocalDateTime.now()
        );

        doNothing().when(seatService).cancelSeat(100L, "1A");

        // When
        seatEventListener.handleReservationCancelled(event);

        // Then
        verify(seatService).cancelSeat(100L, "1A");
    }

    @Test
    @DisplayName("여러 이벤트 처리")
    void handleMultipleEvents() {
        // Given
        ReservationRequestedEvent event1 = new ReservationRequestedEvent(1L, 100L, "1A", LocalDateTime.now());
        ReservationRequestedEvent event2 = new ReservationRequestedEvent(2L, 100L, "1B", LocalDateTime.now());
        ReservationCancelledEvent event3 = new ReservationCancelledEvent(1L, 100L, "1A", LocalDateTime.now());

        doNothing().when(seatService).reserveSeat(anyLong(), anyString());
        doNothing().when(seatService).cancelSeat(anyLong(), anyString());

        // When
        seatEventListener.handleReservationRequested(event1);
        seatEventListener.handleReservationRequested(event2);
        seatEventListener.handleReservationCancelled(event3);

        // Then
        verify(seatService).reserveSeat(100L, "1A");
        verify(seatService).reserveSeat(100L, "1B");
        verify(seatService).cancelSeat(100L, "1A");
    }
}

package com.ktcloudinfra.trainservice.listener;

import com.ktcloudinfra.trainservice.dto.event.ReservationCancelledEvent;
import com.ktcloudinfra.trainservice.dto.event.ReservationRequestedEvent;
import com.ktcloudinfra.trainservice.service.TrainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainEventListenerTest {

    @Mock
    private TrainService trainService;

    @InjectMocks
    private TrainEventListener trainEventListener;

    @Test
    @DisplayName("예약 요청 이벤트 처리 - 좌석 감소")
    void handleReservationRequested() {
        // Given
        ReservationRequestedEvent event = new ReservationRequestedEvent(
                1L,    // reservationId
                100L,  // trainId
                "1A",  // seatNumber
                LocalDateTime.now()
        );

        doNothing().when(trainService).decrementAvailableSeats(100L);

        // When
        trainEventListener.handleReservationRequested(event);

        // Then
        verify(trainService).decrementAvailableSeats(100L);
    }

    @Test
    @DisplayName("예약 취소 이벤트 처리 - 좌석 증가")
    void handleReservationCancelled() {
        // Given
        ReservationCancelledEvent event = new ReservationCancelledEvent(
                1L,    // reservationId
                100L,  // trainId
                "1A",  // seatNumber
                LocalDateTime.now()
        );

        doNothing().when(trainService).incrementAvailableSeats(100L);

        // When
        trainEventListener.handleReservationCancelled(event);

        // Then
        verify(trainService).incrementAvailableSeats(100L);
    }

    @Test
    @DisplayName("여러 예약 요청 이벤트 처리")
    void handleMultipleReservationRequests() {
        // Given
        ReservationRequestedEvent event1 = new ReservationRequestedEvent(1L, 100L, "1A", LocalDateTime.now());
        ReservationRequestedEvent event2 = new ReservationRequestedEvent(2L, 100L, "1B", LocalDateTime.now());
        ReservationRequestedEvent event3 = new ReservationRequestedEvent(3L, 101L, "2A", LocalDateTime.now());

        doNothing().when(trainService).decrementAvailableSeats(anyLong());

        // When
        trainEventListener.handleReservationRequested(event1);
        trainEventListener.handleReservationRequested(event2);
        trainEventListener.handleReservationRequested(event3);

        // Then
        verify(trainService, times(2)).decrementAvailableSeats(100L);
        verify(trainService, times(1)).decrementAvailableSeats(101L);
    }

    @Test
    @DisplayName("예약과 취소 이벤트 혼합 처리")
    void handleMixedEvents() {
        // Given
        ReservationRequestedEvent requestEvent = new ReservationRequestedEvent(
                1L, 100L, "1A", LocalDateTime.now()
        );
        ReservationCancelledEvent cancelEvent = new ReservationCancelledEvent(
                1L, 100L, "1A", LocalDateTime.now()
        );

        doNothing().when(trainService).decrementAvailableSeats(100L);
        doNothing().when(trainService).incrementAvailableSeats(100L);

        // When
        trainEventListener.handleReservationRequested(requestEvent);
        trainEventListener.handleReservationCancelled(cancelEvent);

        // Then
        verify(trainService).decrementAvailableSeats(100L);
        verify(trainService).incrementAvailableSeats(100L);
    }
}

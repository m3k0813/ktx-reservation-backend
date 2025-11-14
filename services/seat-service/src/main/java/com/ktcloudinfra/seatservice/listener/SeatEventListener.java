package com.ktcloudinfra.seatservice.listener;

import com.ktcloudinfra.seatservice.config.RabbitMQConfig;
import com.ktcloudinfra.seatservice.dto.event.ReservationCancelledEvent;
import com.ktcloudinfra.seatservice.dto.event.ReservationRequestedEvent;
import com.ktcloudinfra.seatservice.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatEventListener {

    private final SeatService seatService;

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_REQUESTED_QUEUE)
    public void handleReservationRequested(ReservationRequestedEvent event) {
        log.info("좌석 예약 이벤트 수신: trainId={}, seatNumber={}",
                event.getTrainId(), event.getSeatNumber());

        try {
            seatService.reserveSeat(event.getTrainId(), event.getSeatNumber());
            log.info("좌석 예약 완료: {}", event.getSeatNumber());
        } catch (Exception e) {
            log.error("좌석 예약 실패: {}", e.getMessage());
            // 보상 트랜잭션 이벤트 발행 (향후 구현)
        }
    }

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_CANCELLED_QUEUE)
    public void handleReservationCancelled(ReservationCancelledEvent event) {
        log.info("좌석 취소 이벤트 수신: trainId={}, seatNumber={}",
                event.getTrainId(), event.getSeatNumber());

        seatService.cancelSeat(event.getTrainId(), event.getSeatNumber());
        log.info("좌석 취소 완료: {}", event.getSeatNumber());
    }
}

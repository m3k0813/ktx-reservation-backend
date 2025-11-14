package com.ktcloudinfra.trainservice.listener;

import com.ktcloudinfra.trainservice.config.RabbitMQConfig;
import com.ktcloudinfra.trainservice.dto.event.ReservationCancelledEvent;
import com.ktcloudinfra.trainservice.dto.event.ReservationRequestedEvent;
import com.ktcloudinfra.trainservice.service.TrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainEventListener {

    private final TrainService trainService;

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_REQUESTED_QUEUE)
    public void handleReservationRequested(ReservationRequestedEvent event) {
        log.info("열차 좌석 업데이트 이벤트 수신: trainId={}", event.getTrainId());
        trainService.decrementAvailableSeats(event.getTrainId());
        log.info("열차 좌석 감소 완료: trainId={}", event.getTrainId());
    }

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_CANCELLED_QUEUE)
    public void handleReservationCancelled(ReservationCancelledEvent event) {
        log.info("열차 좌석 복구 이벤트 수신: trainId={}", event.getTrainId());
        trainService.incrementAvailableSeats(event.getTrainId());
        log.info("열차 좌석 증가 완료: trainId={}", event.getTrainId());
    }
}

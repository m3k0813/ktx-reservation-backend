package com.ktcloudinfra.seatservice.service;

import com.ktcloudinfra.seatservice.dto.response.SeatResponseDTO;

import java.util.List;

public interface SeatService {
    List<SeatResponseDTO> getSeatsByTrain(Long trainId);

    void reserveSeat(Long seatId);

    void cancelSeat(Long seatId);

    // Event-based methods
    void reserveSeat(Long trainId, String seatNumber);

    void cancelSeat(Long trainId, String seatNumber);
}

package com.ktcloudinfra.seatservice.repository;

import com.ktcloudinfra.seatservice.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByTrainIdAndSeatNumber(Long trainId, String seatNumber);

    List<Seat> findByTrainId(Long trainId);
}

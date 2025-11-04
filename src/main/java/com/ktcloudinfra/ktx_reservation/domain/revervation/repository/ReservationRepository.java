package com.ktcloudinfra.ktx_reservation.domain.revervation.repository;

import com.ktcloudinfra.ktx_reservation.domain.revervation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
}

package com.ktcloudinfra.ktx_reservation.domain.revervation.repository;

import com.ktcloudinfra.ktx_reservation.domain.revervation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}

package com.ktcloudinfra.ktx_reservation.domain.train.repository;

import com.ktcloudinfra.ktx_reservation.domain.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {
}

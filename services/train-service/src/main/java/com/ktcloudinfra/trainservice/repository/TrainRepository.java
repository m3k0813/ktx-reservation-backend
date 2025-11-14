package com.ktcloudinfra.trainservice.repository;

import com.ktcloudinfra.trainservice.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {
}

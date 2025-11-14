package com.ktcloudinfra.trainservice.repository;

import com.ktcloudinfra.trainservice.entity.Train;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TrainRepositoryTest {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("열차 저장 및 ID로 조회")
    void saveAndFindById() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Train train = Train.builder()
                .name("KTX-101")
                .price(50000)
                .departureStation("서울")
                .arrivalStation("부산")
                .departureTime(now)
                .arrivalTime(now.plusHours(3))
                .availableSeats(100)
                .build();

        // When
        Train saved = trainRepository.save(train);
        Optional<Train> found = trainRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("KTX-101");
        assertThat(found.get().getPrice()).isEqualTo(50000);
        assertThat(found.get().getAvailableSeats()).isEqualTo(100);
    }

    @Test
    @DisplayName("모든 열차 조회")
    void findAll() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Train train1 = Train.builder()
                .name("KTX-101")
                .price(50000)
                .departureStation("서울")
                .arrivalStation("부산")
                .departureTime(now)
                .arrivalTime(now.plusHours(3))
                .availableSeats(100)
                .build();
        Train train2 = Train.builder()
                .name("KTX-102")
                .price(45000)
                .departureStation("서울")
                .arrivalStation("대전")
                .departureTime(now.plusHours(1))
                .arrivalTime(now.plusHours(2))
                .availableSeats(80)
                .build();

        entityManager.persist(train1);
        entityManager.persist(train2);
        entityManager.flush();

        // When
        List<Train> trains = trainRepository.findAll();

        // Then
        assertThat(trains).hasSize(2);
        assertThat(trains).extracting(Train::getName)
                .containsExactlyInAnyOrder("KTX-101", "KTX-102");
    }

    @Test
    @DisplayName("열차 조회 실패 - 존재하지 않음")
    void findById_NotFound() {
        // When
        Optional<Train> found = trainRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("열차 업데이트")
    void updateTrain() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Train train = Train.builder()
                .name("KTX-101")
                .price(50000)
                .departureStation("서울")
                .arrivalStation("부산")
                .departureTime(now)
                .arrivalTime(now.plusHours(3))
                .availableSeats(100)
                .build();
        Train saved = entityManager.persist(train);
        entityManager.flush();

        // When
        saved.updateAvailableSeats(90);
        entityManager.flush();
        entityManager.clear();

        // Then
        Train updated = trainRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getAvailableSeats()).isEqualTo(90);
    }
}

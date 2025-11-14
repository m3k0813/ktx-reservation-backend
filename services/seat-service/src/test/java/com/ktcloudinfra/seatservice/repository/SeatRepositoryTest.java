package com.ktcloudinfra.seatservice.repository;

import com.ktcloudinfra.seatservice.entity.Seat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("열차 ID로 좌석 목록 조회")
    void findByTrainId() {
        // Given
        Long trainId = 100L;
        Seat seat1 = Seat.builder().seatNumber("1A").trainId(trainId).build();
        Seat seat2 = Seat.builder().seatNumber("1B").trainId(trainId).build();
        Seat seat3 = Seat.builder().seatNumber("2A").trainId(200L).build();

        entityManager.persist(seat1);
        entityManager.persist(seat2);
        entityManager.persist(seat3);
        entityManager.flush();

        // When
        List<Seat> seats = seatRepository.findByTrainId(trainId);

        // Then
        assertThat(seats).hasSize(2);
        assertThat(seats).extracting(Seat::getSeatNumber)
                .containsExactlyInAnyOrder("1A", "1B");
    }

    @Test
    @DisplayName("열차 ID로 좌석 조회 - 결과 없음")
    void findByTrainId_NoResults() {
        // When
        List<Seat> seats = seatRepository.findByTrainId(999L);

        // Then
        assertThat(seats).isEmpty();
    }

    @Test
    @DisplayName("열차 ID와 좌석번호로 좌석 조회")
    void findByTrainIdAndSeatNumber() {
        // Given
        Long trainId = 100L;
        Seat seat = Seat.builder().seatNumber("1A").trainId(trainId).build();
        entityManager.persist(seat);
        entityManager.flush();

        // When
        Optional<Seat> found = seatRepository.findByTrainIdAndSeatNumber(trainId, "1A");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSeatNumber()).isEqualTo("1A");
        assertThat(found.get().getTrainId()).isEqualTo(trainId);
    }

    @Test
    @DisplayName("열차 ID와 좌석번호로 좌석 조회 - 존재하지 않음")
    void findByTrainIdAndSeatNumber_NotFound() {
        // When
        Optional<Seat> found = seatRepository.findByTrainIdAndSeatNumber(999L, "1A");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("좌석 저장 및 예약 상태 변경")
    void saveAndReserve() {
        // Given
        Seat seat = Seat.builder().seatNumber("1A").trainId(100L).build();
        Seat saved = entityManager.persist(seat);
        entityManager.flush();

        assertThat(saved.isReserved()).isFalse();

        // When
        saved.reserve();
        entityManager.flush();
        entityManager.clear();

        // Then
        Seat updated = seatRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.isReserved()).isTrue();
    }

    @Test
    @DisplayName("좌석 예약 후 취소")
    void reserveAndCancel() {
        // Given
        Seat seat = Seat.builder().seatNumber("1A").trainId(100L).build();
        Seat saved = entityManager.persist(seat);
        saved.reserve();
        entityManager.flush();
        entityManager.clear();

        // When
        Seat reserved = seatRepository.findById(saved.getId()).orElseThrow();
        reserved.cancel();
        entityManager.flush();
        entityManager.clear();

        // Then
        Seat cancelled = seatRepository.findById(saved.getId()).orElseThrow();
        assertThat(cancelled.isReserved()).isFalse();
    }
}

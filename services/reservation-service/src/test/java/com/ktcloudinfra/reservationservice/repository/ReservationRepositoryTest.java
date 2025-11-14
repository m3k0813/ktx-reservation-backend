package com.ktcloudinfra.reservationservice.repository;

import com.ktcloudinfra.reservationservice.entity.Reservation;
import com.ktcloudinfra.reservationservice.entity.ReservationStatus;
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
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("예약 저장 및 ID로 조회")
    void saveAndFindById() {
        // Given
        Reservation reservation = Reservation.builder()
                .userId(1L)
                .trainId(100L)
                .seatId(10L)
                .seatNumber("1A")
                .status(ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now())
                .build();

        // When
        Reservation saved = reservationRepository.save(reservation);
        Optional<Reservation> found = reservationRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getTrainId()).isEqualTo(100L);
        assertThat(found.get().getSeatNumber()).isEqualTo("1A");
        assertThat(found.get().getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("사용자 ID로 예약 목록 조회")
    void findByUserId() {
        // Given
        Long userId = 1L;
        Reservation res1 = Reservation.builder()
                .userId(userId)
                .trainId(100L)
                .seatId(10L)
                .seatNumber("1A")
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.now())
                .build();
        Reservation res2 = Reservation.builder()
                .userId(userId)
                .trainId(101L)
                .seatId(11L)
                .seatNumber("2B")
                .status(ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now())
                .build();
        Reservation res3 = Reservation.builder()
                .userId(2L)
                .trainId(102L)
                .seatId(12L)
                .seatNumber("3C")
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.now())
                .build();

        entityManager.persist(res1);
        entityManager.persist(res2);
        entityManager.persist(res3);
        entityManager.flush();

        // When
        List<Reservation> reservations = reservationRepository.findByUserId(userId);

        // Then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).extracting(Reservation::getSeatNumber)
                .containsExactlyInAnyOrder("1A", "2B");
    }

    @Test
    @DisplayName("사용자 ID로 예약 조회 - 결과 없음")
    void findByUserId_NoResults() {
        // When
        List<Reservation> reservations = reservationRepository.findByUserId(999L);

        // Then
        assertThat(reservations).isEmpty();
    }

    @Test
    @DisplayName("예약 상태 업데이트")
    void updateStatus() {
        // Given
        Reservation reservation = Reservation.builder()
                .userId(1L)
                .trainId(100L)
                .seatId(10L)
                .seatNumber("1A")
                .status(ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now())
                .build();
        Reservation saved = entityManager.persist(reservation);
        entityManager.flush();

        // When
        saved.updateStatus(ReservationStatus.CONFIRMED);
        entityManager.flush();
        entityManager.clear();

        // Then
        Reservation updated = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("예약 삭제")
    void deleteReservation() {
        // Given
        Reservation reservation = Reservation.builder()
                .userId(1L)
                .trainId(100L)
                .seatId(10L)
                .seatNumber("1A")
                .status(ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now())
                .build();
        Reservation saved = reservationRepository.save(reservation);

        // When
        reservationRepository.delete(saved);

        // Then
        Optional<Reservation> deleted = reservationRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }
}

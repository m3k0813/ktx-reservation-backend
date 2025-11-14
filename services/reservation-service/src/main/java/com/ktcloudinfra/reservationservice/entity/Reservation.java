package com.ktcloudinfra.reservationservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "train_id", nullable = false)
    private Long trainId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Builder
    public Reservation(Long userId, Long trainId, Long seatId, String seatNumber, ReservationStatus status, LocalDateTime reservedAt) {
        this.userId = userId;
        this.trainId = trainId;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.status = status;
        this.reservedAt = reservedAt;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }
}

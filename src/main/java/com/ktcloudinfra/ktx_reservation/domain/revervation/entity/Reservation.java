package com.ktcloudinfra.ktx_reservation.domain.revervation.entity;

import com.ktcloudinfra.ktx_reservation.domain.seat.entity.Seat;
import com.ktcloudinfra.ktx_reservation.domain.train.entity.Train;
import com.ktcloudinfra.ktx_reservation.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id")
    private Train train;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Builder
    public Reservation(User user, Train train, LocalDateTime reservedAt, Seat seat) {
        this.user = user;
        this.train = train;
        this.reservedAt = reservedAt;
        this.seat = seat;
    }
}
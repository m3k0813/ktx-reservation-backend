package com.ktcloudinfra.ktx_reservation.domain.seat.entity;

import com.ktcloudinfra.ktx_reservation.domain.train.entity.Train;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false)
    private boolean reserved = false;

    @Builder
    public Seat(String seatNumber, Train train) {
        this.seatNumber = seatNumber;
        this.train = train;
        this.reserved = false;
    }

    public void reserve() {
        this.reserved = true;
    }

    public void cancel() {
        this.reserved = false;
    }
}

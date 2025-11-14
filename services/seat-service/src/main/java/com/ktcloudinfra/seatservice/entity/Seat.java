package com.ktcloudinfra.seatservice.entity;

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

    @Column(name = "train_id", nullable = false)
    private Long trainId;

    @Column(nullable = false)
    private boolean reserved = false;

    @Builder
    public Seat(String seatNumber, Long trainId) {
        this.seatNumber = seatNumber;
        this.trainId = trainId;
        this.reserved = false;
    }

    public void reserve() {
        this.reserved = true;
    }

    public void cancel() {
        this.reserved = false;
    }
}

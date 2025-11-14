package com.ktcloudinfra.seatservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatResponseDTO {
    private Long id;
    private String name;
    private String seatNumber;
    private boolean reserved;
}

package com.jakdang.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 새로운 공간 예약을 요청할 때 사용하는 DTO 입니다.
 */
@Getter
@Setter
public class CreateBookingRequest {
    private Long spaceId; // 예약할 공간의 ID
    private Long teamId; // (선택) 예약하는 팀의 ID
    private LocalDateTime startTime; // 예약 시작 시간
    private LocalDateTime endTime; // 예약 종료 시간
}

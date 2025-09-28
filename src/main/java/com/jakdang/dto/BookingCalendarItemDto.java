package com.jakdang.dto;

import java.time.LocalDateTime;

/**
 * 달력/타임라인용 공개 DTO (타인 예약은 개인정보 최소화)
 */
public record BookingCalendarItemDto(
        Long bookingId,
        Long spaceId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean mine,          // 내 예약 여부
        String displayTitle    // mine이면 제목/“내 예약”, 아니면 "예약됨"
) {}
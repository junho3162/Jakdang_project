package com.jakdang.dto;

import java.util.List;

/**
 * 가용성 체크 결과 DTO
 */
public record AvailabilityCheckResponse(
        boolean available,
        List<BookingCalendarItemDto> conflicts
) {}
package com.jakdang.dto;

import com.jakdang.domain.Booking;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 예약 정보를 클라이언트에게 응답할 때 사용하는 DTO 입니다.
 */
@Getter
public class BookingResponse {

    private final Long bookingId;
    private final String spaceName;
    private final String bookerNickname; // 예약자 닉네임
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;


    public BookingResponse(Booking booking) {
        this.bookingId = booking.getId();
        this.spaceName = booking.getSpace().getSpaceName();
        this.bookerNickname = booking.getUser().getNickname();
        this.startTime = booking.getStartTime();
        this.endTime = booking.getEndTime();
    }
}

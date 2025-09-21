package com.jakdang.controller;

import com.jakdang.domain.Booking;
import com.jakdang.dto.BookingResponse;
import com.jakdang.dto.CreateBookingRequest;
import com.jakdang.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    /**
     * 새로운 공간 예약을 생성하는 API 입니다.
     * @param request 예약 정보
     * @param authentication 현재 로그인한 사용자 정보
     * @return 생성된 예약의 상세 정보
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Booking newBooking = bookingService.createBooking(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(new BookingResponse(newBooking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

package com.jakdang.controller;

import com.jakdang.domain.Booking;
import com.jakdang.dto.AvailabilityCheckResponse;
import com.jakdang.dto.BookingCalendarItemDto;
import com.jakdang.dto.BookingResponse;
import com.jakdang.dto.CreateBookingRequest;
import com.jakdang.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 예약 목록 조회 API
     * - 기본: 로그인 사용자 본인의 모든 예약 반환
     * - 필터: spaceId + (date) 또는 spaceId + (start~end)로 기간 필터링
     *   예1) GET /api/bookings?spaceId=1&date=2025-09-27
     *   예2) GET /api/bookings?spaceId=1&start=2025-09-27T09:00:00&end=2025-09-27T21:00:00
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getMyBookings(@RequestParam(required = false) Long spaceId,
            Authentication authentication) {
        String me = authentication.getName();
        return ResponseEntity.ok(bookingService.getMyBookings(me, spaceId));
    }

    /**
     * (현황) 특정 공간의 예약 현황 (내 것 + 타인 것)
     * - 하루:   /api/bookings/status?spaceId=1&date=2025-09-28
     * - 구간:   /api/bookings/status?spaceId=1&start=2025-09-28T09:00:00&end=2025-09-28T21:00:00
     * - 기본값: 오늘 00:00 ~ 내일 00:00
     * - 응답: BookingCalendarItemDto(타인은 "예약됨"으로 마스킹)
     */
    @GetMapping("/status")
    public ResponseEntity<List<BookingCalendarItemDto>> getStatus(@RequestParam Long spaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {
        String me = authentication.getName();
        LocalDateTime s, e;
        if (date != null) {
            s = date.atStartOfDay();
            e = date.plusDays(1).atStartOfDay();
        } else if (start != null && end != null && start.isBefore(end)) {
            s = start; e = end;
        } else {
            LocalDate today = LocalDate.now();
            s = today.atStartOfDay();
            e = today.plusDays(1).atStartOfDay();
        }
        return ResponseEntity.ok(bookingService.getStatus(spaceId, s, e, me));
    }

    /**
     * (가용성) 해당 구간 예약 가능 여부 + 충돌 목록
     * - 예: /api/bookings/check?spaceId=1&start=2025-09-28T13:00:00&end=2025-09-28T15:00:00
     */
    @GetMapping("/check")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(@RequestParam Long spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {
        if (!start.isBefore(end)) return ResponseEntity.badRequest().build();
        String me = authentication.getName();
        return ResponseEntity.ok(bookingService.checkAvailability(spaceId, start, end, me));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            bookingService.cancelBooking(bookingId, userEmail);

            // [중요] 여기서 .noContent()를 쓰면 204가 되고 메시지가 안 나옵니다.
            // 아래처럼 .ok("메시지")를 써야 200이 되고 메시지가 나옵니다!
            return ResponseEntity.ok("예약이 성공적으로 취소되었습니다.");

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("예약 취소 중 오류가 발생했습니다.");
        }
    }
}

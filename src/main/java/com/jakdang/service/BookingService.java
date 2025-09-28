package com.jakdang.service;

import com.jakdang.domain.Booking;
import com.jakdang.domain.Space;
import com.jakdang.domain.User;
import com.jakdang.dto.AvailabilityCheckResponse;
import com.jakdang.dto.BookingCalendarItemDto;
import com.jakdang.dto.BookingResponse;
import com.jakdang.dto.CreateBookingRequest;
import com.jakdang.repository.BookingRepository;
import com.jakdang.repository.SpaceRepository;
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공간 예약 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    /**
     * 새로운 공간 예약을 생성하는 메소드입니다.
     * @param request 예약 생성에 필요한 정보
     * @param userEmail 예약을 요청한 사용자의 이메일
     * @return 생성된 Booking 엔티티
     */
    @Transactional
    public Booking createBooking(CreateBookingRequest request, String userEmail) {
        // 예약자 정보를 조회합니다.
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));

        // 예약할 공간 정보를 조회합니다.
        Space space = spaceRepository.findById(request.getSpaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공간을 찾을 수 없습니다: " + request.getSpaceId()));

        // (핵심!) 요청된 시간대에 이미 다른 예약이 있는지 확인합니다.
        List<Booking> overlappingBookings = bookingRepository.findBySpaceIdAndStartTimeBeforeAndEndTimeAfter(
                request.getSpaceId(), request.getEndTime(), request.getStartTime());
        if (!overlappingBookings.isEmpty()) {
            throw new IllegalStateException("해당 시간대에는 이미 예약이 존재합니다.");
        }

        // 예약 엔티티를 생성합니다. (팀 예약은 향후 구현)
        Booking booking = Booking.builder()
                .user(user)
                .space(space)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        return bookingRepository.save(booking);
    }

    /** (내 예약) 목록 조회 */
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String userEmail, Long spaceIdOrNull) {
        List<Booking> list = (spaceIdOrNull == null)
                ? bookingRepository.findByUserEmailOrderByStartTimeAsc(userEmail)
                : bookingRepository.findByUserEmailAndSpaceIdOrderByStartTimeAsc(userEmail, spaceIdOrNull);
        return list.stream().map(BookingResponse::new).toList();
    }

    /** (현황) 특정 공간의 [start, end) 구간 겹치는 모든 예약 + mine 플래그/마스킹 */
    @Transactional(readOnly = true)
    public List<BookingCalendarItemDto> getStatus(Long spaceId,
                                                  LocalDateTime startInclusive,
                                                  LocalDateTime endExclusive,
                                                  String currentUserEmail) {
        List<Booking> overlaps =
                bookingRepository.findBySpaceIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeAsc(
                        spaceId, endExclusive, startInclusive);
        return overlaps.stream().map(b -> toCalendarItem(b, currentUserEmail)).toList();
    }

    /** (가용성) 해당 구간이 비어있는지 + 충돌 목록 */
    @Transactional(readOnly = true)
    public AvailabilityCheckResponse checkAvailability(Long spaceId,
                                                       LocalDateTime startInclusive,
                                                       LocalDateTime endExclusive,
                                                       String currentUserEmail) {
        List<Booking> overlaps =
                bookingRepository.findBySpaceIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeAsc(
                        spaceId, endExclusive, startInclusive);
        boolean available = overlaps.isEmpty();
        List<BookingCalendarItemDto> conflicts =
                overlaps.stream().map(b -> toCalendarItem(b, currentUserEmail)).toList();
        return new AvailabilityCheckResponse(available, conflicts);
    }

    private BookingCalendarItemDto toCalendarItem(Booking b, String me) {
        boolean mine = b.getUser().getEmail().equals(me);
        String displayTitle = mine ? "내 예약" : "예약됨";
        return new BookingCalendarItemDto(
                b.getId(),
                b.getSpace().getId(),
                b.getStartTime(),
                b.getEndTime(),
                mine,
                displayTitle
        );
        // 만약 '내 예약'에 개별 제목을 넣고 싶다면 Booking 엔티티에 title 필드 추가 후 여기서 분기하세요.
    }


}

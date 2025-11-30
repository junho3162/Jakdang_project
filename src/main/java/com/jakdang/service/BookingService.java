package com.jakdang.service;

import com.jakdang.domain.Booking;
import com.jakdang.domain.Space;
import com.jakdang.domain.Team; // Team 추가
import com.jakdang.domain.User;
import com.jakdang.dto.AvailabilityCheckResponse;
import com.jakdang.dto.BookingCalendarItemDto;
import com.jakdang.dto.BookingResponse;
import com.jakdang.dto.CreateBookingRequest;
import com.jakdang.repository.BookingRepository;
import com.jakdang.repository.SpaceRepository;
import com.jakdang.repository.TeamRepository; // TeamRepo 추가
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final TeamRepository teamRepository; // [추가]

    @Transactional
    public Booking createBooking(CreateBookingRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));

        Space space = spaceRepository.findById(request.getSpaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공간을 찾을 수 없습니다: " + request.getSpaceId()));

        List<Booking> overlappingBookings = bookingRepository.findBySpaceIdAndStartTimeBeforeAndEndTimeAfter(
                request.getSpaceId(), request.getEndTime(), request.getStartTime());
        if (!overlappingBookings.isEmpty()) {
            throw new IllegalStateException("해당 시간대에는 이미 예약이 존재합니다.");
        }

        // [수정됨] 팀 정보가 있으면 조회해서 넣어줍니다.
        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        }

        Booking booking = Booking.builder()
                .user(user)
                .space(space)
                .team(team) // [중요] 팀 정보 저장!
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


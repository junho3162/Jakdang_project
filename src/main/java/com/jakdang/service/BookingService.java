package com.jakdang.service;

import com.jakdang.domain.Booking;
import com.jakdang.domain.Space;
import com.jakdang.domain.User;
import com.jakdang.dto.CreateBookingRequest;
import com.jakdang.repository.BookingRepository;
import com.jakdang.repository.SpaceRepository;
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
}

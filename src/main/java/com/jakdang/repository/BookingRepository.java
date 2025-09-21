package com.jakdang.repository;

import com.jakdang.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * 특정 공간에 대해, 주어진 시간 범위와 겹치는 예약이 있는지 조회합니다.
     * 이 메소드는 중복 예약을 방지하는 데 핵심적인 역할을 합니다.
     * @param spaceId 확인할 공간의 ID
     * @param startTime 확인할 시간 범위의 시작
     * @param endTime 확인할 시간 범위의 끝
     * @return 겹치는 예약 목록
     */
    List<Booking> findBySpaceIdAndStartTimeBeforeAndEndTimeAfter(Long spaceId, LocalDateTime endTime, LocalDateTime startTime);
}

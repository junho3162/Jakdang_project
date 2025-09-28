package com.jakdang.repository;

import com.jakdang.domain.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
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

    // 내 예약 전체(옵션: 특정 공간) - 달력/리스트용
    @EntityGraph(attributePaths = {"space", "user"})
    List<Booking> findByUserEmailOrderByStartTimeAsc(String email);

    @EntityGraph(attributePaths = {"space", "user"})
    List<Booking> findByUserEmailAndSpaceIdOrderByStartTimeAsc(String email, Long spaceId);

    // 현황/가용성 조회 시, N+1 방지 위해 fetch join 대체용 EntityGraph
    @EntityGraph(attributePaths = {"space", "user"})
    List<Booking> findBySpaceIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeAsc(
            Long spaceId, LocalDateTime endExclusive, LocalDateTime startInclusive);
}

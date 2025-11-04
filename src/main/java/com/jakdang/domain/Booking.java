package com.jakdang.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 공간 예약 정보를 담는 Entity 클래스입니다.
 * 데이터베이스의 'bookings' 테이블과 직접 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    // 예약을 한 사용자 정보 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 예약된 공간 정보 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    // (선택) 팀 예약일 경우, 어떤 팀이 예약했는지 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // 예약 시작 시간

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // 예약 종료 시간

    @Builder
    public Booking(User user, Space space, Team team, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.space = space;
        this.team = team;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}

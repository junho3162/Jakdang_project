package com.jakdang.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팀 지원 정보를 담는 Entity 클래스입니다.
 * 데이터베이스의 'applications' 테이블과 직접 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    // 지원자 정보 (N:1 관계)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    // 지원한 팀 정보 (N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // 지원 메시지

    @Column(name = "portfolio_url")
    private String portfolioUrl; // 포트폴리오 파일 경로 (향후 확장용)

    @Column(name = "status")
    private String status; // 지원 상태 (대기중, 수락, 거절)

    @Builder
    public Application(User applicant, Team team, String message) {
        this.applicant = applicant;
        this.team = team;
        this.message = message;
        this.status = "대기중"; // 지원서 생성 시 기본 상태는 '대기중'
    }

    /**
     * 지원서의 상태를 변경하는 메소드입니다.
     * @param newStatus 새로운 상태 ("수락", "거절" 등)
     */
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}


package com.jakdang.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팀 지원서 정보를 담는 Entity 클래스입니다.
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

    // N:1 (다대일) 관계 - 한 명의 사용자는 여러 개의 지원서를 쓸 수 있습니다.
    @ManyToOne(fetch = FetchType.LAZY) // (수정) EAGER -> LAZY로 변경 (성능 최적화)
    @JoinColumn(name = "user_id") // DB에 저장될 외래 키 컬럼 이름
    private User applicant; // 지원자

    // N:1 (다대일) 관계 - 하나의 팀에는 여러 개의 지원서가 올 수 있습니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id") // DB에 저장될 외래 키 컬럼 이름
    private Team team; // 지원한 팀

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // 지원 메시지

    // (핵심!) 'status' 필드
    @Column(name = "status", nullable = false)
    private String status; // 지원 상태 (예: "대기중", "ACCEPTED", "REJECTED")

    // (제거!) 'portfolioUrl' 필드는 현재 기획(DTO)에서 사용되지 않으므로 제거합니다.

    @Builder
    public Application(User applicant, Team team, String message, String status) { // (수정!) Builder가 status를 받도록 변경
        this.applicant = applicant;
        this.team = team;
        this.message = message;
        this.status = status; // (수정!) Builder를 통해 status를 주입받음
    }

    /**
     * 지원서의 상태를 업데이트하는 메소드입니다. (예: "대기중" -> "ACCEPTED")
     */
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}
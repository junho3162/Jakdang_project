package com.jakdang.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 모집 공고(팀) 정보를 담는 Entity 클래스입니다.
 * 데이터베이스의 'teams' 테이블과 직접 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id", updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title; // 공고 제목

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 공고 내용

    @Column(name = "category", nullable = false)
    private String category; // 카테고리 (공모전, 스터디 등)

    @Column(name = "status", nullable = false)
    private String status; // 모집 상태 (모집중, 모집완료)

    @Column(name = "max_members", nullable = false)
    private int maxMembers; // 최대 모집 인원

    // 모집 마감 기한
    @Column(name = "deadline")
    private LocalDate deadline;

    // ✅ 활동/모임 시간 (예: 08:00 ~ 12:00)
    @Column(name = "start_time")
    private LocalTime startTime;   // 시작 시간

    @Column(name = "end_time")
    private LocalTime endTime;     // 종료 시간

    // ✅ 구하는 팀원 역할을 한 번에 적어두는 텍스트
    // 예) "기획 1명, 디자이너 1명, 개발자 2명"
    @Column(name = "recruit_roles", columnDefinition = "TEXT")
    private String recruitRoles;

    // 팀 해시태그
    @ElementCollection(targetClass = TeamTag.class)
    @CollectionTable(
            name = "team_tags",
            joinColumns = @JoinColumn(name = "team_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private Set<TeamTag> tags = new HashSet<>();

    // User와의 다대일(N:1) 관계 설정
    // 여러 개의 팀(Team)은 한 명의 사용자(User)에 의해 생성될 수 있습니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id") // 외래키(FK) 컬럼 이름을 'leader_id'로 지정
    private User leader; // 팀장 정보

    // (향후 확장용) 팀 멤버 목록 - 지금은 사용하지 않지만 미리 구조를 잡아둡니다.
    // @ManyToMany
    // private List<User> members = new ArrayList<>();

    @Builder
    public Team(String title, String content, String category, String status, int maxMembers, User leader,
                Set<TeamTag> tags, LocalDate deadline, LocalTime startTime, LocalTime endTime, String recruitRoles) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.status = status;
        this.maxMembers = maxMembers;
        this.leader = leader;

        if (tags != null) { this.tags = tags; }

        this.deadline = deadline;
        this.startTime = startTime;
        this.endTime = endTime;
        this.recruitRoles = recruitRoles;
    }


    /**
     * DTO의 데이터를 기반으로 자신의 필드를 직접 수정하는 메소드입니다.
     * - 서비스 계층의 코드를 더 깔끔하게 유지할 수 있습니다.
     * - 이 메소드는 @Transactional 환경에서 호출되면 변경된 내용이 자동으로 데이터베이스에 반영됩니다.
     */
    public void update(String title, String content, String category, String status, int maxMembers,
                       LocalDate deadline, Set<TeamTag> tags, LocalTime startTime, LocalTime endTime, String recruitRoles) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.status = status;
        this.maxMembers = maxMembers;
        this.deadline = deadline;

        if (tags != null) {
            this.tags.clear();
            this.tags.addAll(tags);
        }

        this.startTime = startTime;
        this.endTime = endTime;
        this.recruitRoles = recruitRoles;
    }

}

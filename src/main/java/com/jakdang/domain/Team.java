package com.jakdang.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate; // (추가) 기한(날짜)을 저장하기 위해 import
import java.util.ArrayList; // (추가) List import
import java.util.List;       // (추가) List import

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
    private String title; // "제목"

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // "내용"

    @Column(name = "category", nullable = false)
    private String category; // "해시태그" (기획상 1개 선택이므로 String 유지)

    @Column(name = "status", nullable = false)
    private String status; // 모집 상태 (모집중, 모집완료)

    @Column(name = "max_members", nullable = false)
    private int maxMembers; // 최대 모집 인원 (기존 필드 활용)

    // (수정!) FetchType.EAGER로 변경 (팀장 정보는 목록에서 바로 필요)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leader_id")
    private User leader; // 팀장 정보

    // --- (신규) '기한' 필드 추가 ---
    @Column(name = "deadline")
    private LocalDate deadline; // 모집 마감 기한

    // --- (신규) '사진/포트폴리오' 필드 추가 ---
    @Column(name = "file_url")
    private String fileUrl; // 업로드된 파일의 저장 경로 또는 URL

    // --- (신규) '구하는 팀원' 필드 추가 ---
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "team_required_roles", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "role_name")
    private List<String> requiredRoles = new ArrayList<>(); // 예: ["개발자", "디자이너"]


    @Builder
    public Team(String title, String content, String category, String status, int maxMembers, User leader, LocalDate deadline, String fileUrl, List<String> requiredRoles) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.status = status;
        this.maxMembers = maxMembers;
        this.leader = leader;
        this.deadline = deadline; // (추가)
        this.fileUrl = fileUrl; // (추가)
        this.requiredRoles = requiredRoles; // (추가)
    }

    // (수정!) update 메소드에도 신규 필드 추가
    public void update(String title, String content, String category, String status, int maxMembers, LocalDate deadline, List<String> requiredRoles) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.status = status;
        this.maxMembers = maxMembers;
        this.deadline = deadline;
        this.requiredRoles = requiredRoles;
    }

    // (신규) 파일 URL을 설정하는 별도 메소드
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
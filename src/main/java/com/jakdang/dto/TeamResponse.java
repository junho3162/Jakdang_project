package com.jakdang.dto;

import com.jakdang.domain.Team;
import com.jakdang.domain.TeamTag;
import lombok.Getter;
import lombok.Setter; // [필수 추가]
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * 모집 공고(팀) 정보를 클라이언트에게 응답할 때 사용하는 DTO 입니다.
 */
@Getter
public class TeamResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String category;
    private final String status;
    private final int maxMembers;
    private final String leaderNickname; // 팀장의 전체 정보 대신 닉네임만 포함
    private boolean isFavorite; // 즐겨찾기 여부 추가 (기본값 false)
    private LocalDate deadline;
    private Set<TeamTag> tags;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recruitRoles;

    // Team 엔티티를 파라미터로 받아, TeamResponse DTO로 변환하는 생성자입니다.
    public TeamResponse(Team team) {
        this.id = team.getId();
        this.title = team.getTitle();
        this.content = team.getContent();
        this.category = team.getCategory();
        this.status = team.getStatus();
        this.maxMembers = team.getMaxMembers();
        this.deadline = team.getDeadline();
        this.tags = team.getTags();
        this.leaderNickname = team.getLeader().getNickname();
        this.isFavorite = false; // 기본적으로 false로 초기화

        this.startTime = team.getStartTime();
        this.endTime = team.getEndTime();
        this.recruitRoles = team.getRecruitRoles();

    }
}

package com.jakdang.dto;

import com.jakdang.domain.Team;
import lombok.Getter;

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

    // Team 엔티티를 파라미터로 받아, TeamResponse DTO로 변환하는 생성자입니다.
    public TeamResponse(Team team) {
        this.id = team.getId();
        this.title = team.getTitle();
        this.content = team.getContent();
        this.category = team.getCategory();
        this.status = team.getStatus();
        this.maxMembers = team.getMaxMembers();
        this.leaderNickname = team.getLeader().getNickname();
    }
}

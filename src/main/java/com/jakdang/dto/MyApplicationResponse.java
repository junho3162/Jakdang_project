package com.jakdang.dto;

import com.jakdang.domain.Application;
import lombok.Getter;

/**
 * '내 지원 현황' 조회 시, 클라이언트에게 응답할 DTO 입니다.
 * 내가 지원한 팀의 정보와, 나의 지원 상태를 포함합니다.
 */
@Getter
public class MyApplicationResponse {

    private final Long applicationId; // 지원서 ID
    private final String status; // 내 지원 상태 (대기중, 수락, 거절)
    private final String message; // 내가 보낸 메시지

    private final Long teamId; // 내가 지원한 팀 ID
    private final String teamTitle; // 내가 지원한 팀 이름
    private final String teamCategory; // 내가 지원한 팀 카테고리
    private final String leaderNickname; // 그 팀의 팀장 닉네임

    public MyApplicationResponse(Application application) {
        this.applicationId = application.getId();
        this.status = application.getStatus();
        this.message = application.getMessage();
        this.teamId = application.getTeam().getId();
        this.teamTitle = application.getTeam().getTitle();
        this.teamCategory = application.getTeam().getCategory();
        this.leaderNickname = application.getTeam().getLeader().getNickname();
    }
}
package com.jakdang.dto;

import com.jakdang.domain.Team;
import lombok.Getter;
import java.time.LocalDate; // (추가)
import java.util.List; // (추가)

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
    private final String leaderNickname;

    // --- (신규) 프로토타입 변경 사항 반영 ---
    private final LocalDate deadline;
    private final String fileUrl;
    private final List<String> requiredRoles;
    // --- ---

    // Team 엔티티를 파라미터로 받아, DTO로 변환하는 생성자
    public TeamResponse(Team team) {
        this.id = team.getId();
        this.title = team.getTitle();
        this.content = team.getContent();
        this.category = team.getCategory();
        this.status = team.getStatus();
        this.maxMembers = team.getMaxMembers();
        this.leaderNickname = team.getLeader().getNickname();
        this.deadline = team.getDeadline(); // (추가)
        this.fileUrl = team.getFileUrl(); // (추가)
        this.requiredRoles = team.getRequiredRoles(); // (추가)
    }
}


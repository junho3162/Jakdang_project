package com.jakdang.dto;

import com.jakdang.domain.Team;
import com.jakdang.domain.User;
import lombok.Getter;
import lombok.Setter;

/**
 * 새로운 모집 공고(팀) 생성을 요청할 때 사용하는 DTO 입니다.
 */
@Getter
@Setter
public class CreateTeamRequest {

    private String title;
    private String content;
    private String category;
    private int maxMembers;

    // 이 DTO를 Team 엔티티로 변환하는 메소드입니다.
    // 팀장(leader) 정보를 받아와 함께 저장합니다.
    public Team toEntity(User leader) {
        return Team.builder()
                .title(title)
                .content(content)
                .category(category)
                .maxMembers(maxMembers)
                .leader(leader)
                .status("모집중") // 공고 생성 시 기본 상태는 '모집중'
                .build();
    }
}

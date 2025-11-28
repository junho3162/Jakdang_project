package com.jakdang.dto;

import com.jakdang.domain.Team;
import com.jakdang.domain.TeamTag;
import com.jakdang.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

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


    private LocalDate deadline;
    private Set<TeamTag> tags;

    private LocalTime startTime;   // 8:00 같은 시작 시간
    private LocalTime endTime;     // 끝나는 시간

    // UI에서 입력하는 "구하는 직책" 전체 텍스트
    // 예: "기획 1명, 디자이너 1명, 개발자 2명"
    private String recruitRoles;

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
                .tags(tags)
                .deadline(deadline)
                .startTime(startTime)
                .endTime(endTime)
                .recruitRoles(recruitRoles)
                .build();
    }
}

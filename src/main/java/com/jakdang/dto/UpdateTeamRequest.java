package com.jakdang.dto;

import com.jakdang.domain.TeamTag;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * 모집 공고(팀) 수정을 요청할 때 사용하는 DTO 입니다.
 */
@Getter
@Setter
public class UpdateTeamRequest {
    private String title;
    private String content;
    private String category;
    private String status;
    private int maxMembers;
    private Set<TeamTag> tags;
    private LocalDate deadline;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recruitRoles;
}
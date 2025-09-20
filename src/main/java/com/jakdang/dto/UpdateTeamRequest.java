package com.jakdang.dto;

import lombok.Getter;
import lombok.Setter;

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
}
package com.jakdang.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 팀 지원 요청 시 클라이언트로부터 받을 데이터를 담는 DTO 입니다.
 */
@Getter
@Setter
public class ApplyTeamRequest {
    private String message; // 팀장에게 보낼 어필 메시지
}

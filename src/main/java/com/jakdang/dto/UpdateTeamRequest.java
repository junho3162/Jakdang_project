package com.jakdang.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

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

    // --- (신규) 프로토타입 변경 사항 반영 ---
    private LocalDate deadline; // '기한' 필드
    private List<String> requiredRoles; // '구하는 팀원' 역할 목록
    // --- ---

    // (참고) 파일 수정(jpg/pdf)은 이 API에서 처리하지 않습니다.
    // 파일 수정을 원할 경우, '공고 생성'처럼 Multipart/form-data를 사용하는
    // 별도의 API를 만들거나 이 API를 더 복잡하게 수정해야 합니다.
    // 여기서는 텍스트 정보만 수정하는 것으로 가정합니다.
}


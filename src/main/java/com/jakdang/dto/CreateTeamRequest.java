package com.jakdang.dto;

import com.jakdang.domain.Team;
import com.jakdang.domain.User;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate; // (추가) LocalDate import
import java.util.List; // (추가) List import

/**
 * 새로운 모집 공고(팀) 생성을 요청할 때 사용하는 DTO 입니다.
 * (파일 업로드를 위해 JSON이 아닌 @RequestPart("requestDto")로 받게 됩니다)
 */
@Getter
@Setter
public class CreateTeamRequest {

    private String title;
    private String content;
    private String category;
    private int maxMembers; // "구하는 팀원"의 총 인원 수

    // --- (신규) 프로토타입 변경 사항 반영 ---
    private LocalDate deadline; // '기한' 필드
    private List<String> requiredRoles; // '구하는 팀원' 역할 목록 (예: "개발자", "디자이너")
    // --- ---

    /**
     * DTO의 데이터를 Team 엔티티로 변환하는 메소드입니다.
     * @param leader 공고를 작성하는 사용자
     * @param fileUrl 파일 업로드 후 반환된 파일 저장 경로 (Service에서 처리 후 전달)
     * @return
     */
    public Team toEntity(User leader, String fileUrl) {
        return Team.builder()
                .title(title)
                .content(content)
                .category(category)
                .maxMembers(maxMembers)
                .leader(leader)
                .status("모집중") // 공고 생성 시 기본 상태는 '모집중'
                .deadline(deadline) // (추가)
                .requiredRoles(requiredRoles) // (추가)
                .fileUrl(fileUrl) // (추가)
                .build();
    }
}


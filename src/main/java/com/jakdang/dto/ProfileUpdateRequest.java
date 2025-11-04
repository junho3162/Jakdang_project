package com.jakdang.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * 마이페이지 프로필 수정을 요청할 때 사용하는 DTO 입니다.
 */
@Getter
@Setter
public class ProfileUpdateRequest {
    private String grade;
    private String department;
    private List<String> interestTags; // 중복 선택이 가능하므로 List로 받습니다.
}

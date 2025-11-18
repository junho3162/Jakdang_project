package com.jakdang.dto;

import com.jakdang.domain.User;
import com.jakdang.domain.UserTag;
import lombok.Getter;

import java.util.Set;

/**
 * 마이페이지 조회 등 클라이언트에게 사용자 정보를 응답할 때 사용하는 DTO 입니다.
 * 비밀번호와 같은 민감한 정보는 제외하고, 안전한 필드만 포함합니다.
 */
@Getter
public class UserResponse {

    private final String email;
    private final String username;
    private final String nickname;
    private final String grade;
    private final String department;
    private Set<UserTag> tags;

    // User 엔티티 객체를 파라미터로 받아, UserResponse DTO 객체로 변환하는 생성자입니다.
    public UserResponse(User user) {
        this.email = user.getEmail(); // UserDetails의 getUsername()과 동일
        this.username = user.getRealUsername(); // 우리가 따로 만들었던 실제 이름 getter
        this.nickname = user.getNickname();
        this.grade = user.getGrade();
        this.department = user.getDepartment();
        this.tags = user.getTags();
    }
}

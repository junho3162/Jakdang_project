package com.jakdang.dto;

import com.jakdang.domain.User;
import lombok.Getter;
import lombok.Setter;

// Lombok: Getter, Setter 메소드를 자동으로 생성해줍니다.
@Getter
@Setter
public class AddUserRequest {

    private String verificationCode; // 사용자가 입력한 이메일 인증 코드

    // 프론트엔드에서 받을 데이터 필드들
    private String email;
    private String password;
    private String username;
    private String nickname;
    private String grade;
    private String department;

    // DTO 객체를 User Entity 객체로 변환하는 메소드
    // Service 계층에서 이 메소드를 호출하여 DB에 저장할 User 객체를 생성합니다.
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword) // 암호화된 비밀번호를 저장
                .username(username)
                .nickname(nickname)
                .grade(grade)
                .department(department)
                .build();
    }
}


package com.jakdang.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    /**
     * 비밀번호 변경을 요청할 때 사용하는 DTO 입니다.
     */
    private String email;
    private String verificationCode;
    private String newPassword;
}

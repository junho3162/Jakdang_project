package com.jakdang.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 이메일 인증 코드 발송을 요청할 때 사용하는 DTO 입니다.
 */
@Getter
@Setter
public class EmailVerificationRequest {
    private String email;
}

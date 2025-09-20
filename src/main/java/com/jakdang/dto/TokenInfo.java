package com.jakdang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {
    // JWT 인증 타입 (여기서는 Bearer 방식 사용)
    private String grantType;
    // 실제 사용자인증을 위한 Access Token
    private String accessToken;
}

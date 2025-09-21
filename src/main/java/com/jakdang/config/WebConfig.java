package com.jakdang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // "/api/"로 시작하는 모든 요청에 대해
                .allowedOrigins("http://localhost:63342", "null") // 로컬 개발 환경(IntelliJ Live Server, file://)에서의 요청을 허용
                //.allowedOrigins("[https://your-frontend-domain.com](https://your-frontend-domain.com)") // [배포 후] 실제 프론트엔드 도메인 주소로 변경
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용할 HTTP 메소드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 인증 정보(쿠키, 토큰 등) 허용
    }
}
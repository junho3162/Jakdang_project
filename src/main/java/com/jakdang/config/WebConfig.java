package com.jakdang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // "/api/"로 시작하는 모든 요청에 대해
                .allowedOrigins("*") // [수정] 배포 테스트를 위해 모든 주소에서의 접속을 임시 허용
                // 나중에 Netlify 배포 후, 해당 도메인(예: https://my-site.netlify.app)만 허용하도록 수정하는 것이 좋습니다.
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용할 HTTP 메소드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(false); // [수정] allowedOrigins가 "*"일 때는 보안상 Credentials를 false로 설정해야 합니다.
    }
}
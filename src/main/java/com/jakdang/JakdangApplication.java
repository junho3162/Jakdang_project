package com.jakdang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 이 클래스가 Spring Boot 애플리케이션의 메인 클래스임을 나타냅니다.
// 이 어노테이션 하나로 기본적인 자동 설정들이 모두 활성화됩니다.
@SpringBootApplication
public class JakdangApplication {

    // Java 애플리케이션의 시작점(Entry Point)인 main 메소드입니다.
    public static void main(String[] args) {
        // SpringApplication.run() 메소드를 호출하여 내장 웹 서버(Tomcat)를 실행하고
        // Spring Boot 애플리케이션을 시작합니다.
        SpringApplication.run(JakdangApplication.class, args);
    }

}

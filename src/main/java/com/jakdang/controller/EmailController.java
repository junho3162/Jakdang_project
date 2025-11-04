package com.jakdang.controller;

import com.jakdang.dto.EmailVerificationRequest;
import com.jakdang.service.UserService; // 1. UserService를 import합니다.
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    // 2. EmailService와 VerificationService 대신 UserService만 주입받습니다.
    private final UserService userService;

    /**
     * 회원가입을 위한 이메일 인증 코드를 발송하는 API 입니다.
     */
    @PostMapping("/verification-requests")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailVerificationRequest request) {
        String email = request.getEmail();

        // 3. (유지) 이메일 형식 검사는 컨트롤러에서 미리 처리합니다.
        if (!email.endsWith("@office.hanseo.ac.kr")) {
            return ResponseEntity.badRequest().body("한서대학교 오피스 계정(@office.hanseo.ac.kr)만 사용할 수 있습니다.");
        }

        try {
            // 4. (수정) 모든 복잡한 로직을 UserService에 위임합니다.
            userService.sendVerificationCodeForSignup(email);
            return ResponseEntity.ok("인증 코드가 성공적으로 발송되었습니다. 이메일을 확인해주세요.");
        } catch (Exception e) {
            // (UserService에서 "이미 가입된 이메일" 예외 발생 시, 여기서 처리)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 비밀번호 재설정을 위한 이메일 인증 코드를 발송하는 API 입니다.
     */
    @PostMapping("/password/verification-requests")
    public ResponseEntity<String> sendPasswordResetCode(@RequestBody EmailVerificationRequest request) {
        try {
            // 5. (수정) 모든 로직을 UserService에 위임합니다.
            userService.sendVerificationCodeForPasswordReset(request.getEmail());

            // (핵심!) 이메일이 존재하든 안 하든, 항상 동일한 성공 메시지를 반환하여
            // 해커가 이메일 존재 여부를 추측할 수 없도록 합니다.
            return ResponseEntity.ok("인증 코드가 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("이메일 발송 중 오류가 발생했습니다.");
        }
    }
}


package com.jakdang.controller;

import com.jakdang.dto.EmailVerificationRequest;
import com.jakdang.service.EmailService;
import com.jakdang.service.VerificationService;
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

    private final EmailService emailService;
    private final VerificationService verificationService;

    /**
     * 회원가입을 위한 이메일 인증 코드를 발송하는 API 입니다.
     */
    @PostMapping("/verification-requests")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailVerificationRequest request) {
        String email = request.getEmail();
        if (!email.endsWith("@office.hanseo.ac.kr")) {
            return ResponseEntity.badRequest().body("한서대학교 오피스 계정(@office.hanseo.ac.kr)만 사용할 수 있습니다.");
        }
        try {
            String code = verificationService.generateAndStoreCode(email);
            emailService.sendVerificationCode(email, "회원가입 인증 코드", code);
            return ResponseEntity.ok("인증 코드가 성공적으로 발송되었습니다. 이메일을 확인해주세요.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("이메일 발송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 재설정을 위한 이메일 인증 코드를 발송하는 API 입니다.
     * @param request 인증 코드를 받을 이메일 주소가 담긴 DTO
     * @return 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/password/verification-requests")
    public ResponseEntity<String> sendPasswordResetCode(@RequestBody EmailVerificationRequest request) {
        String email = request.getEmail();
        try {
            String code = verificationService.generateAndStoreCode(email);
            emailService.sendVerificationCode(email, "비밀번호 재설정 인증 코드", code);
            return ResponseEntity.ok("인증 코드가 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("이메일 발송 중 오류가 발생했습니다.");
        }
    }
}


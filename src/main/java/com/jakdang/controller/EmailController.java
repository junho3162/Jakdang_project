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

        // [수정] 개인 메일 테스트를 위해 도메인 검사 로직을 잠시 주석 처리함
        // if (!email.endsWith("@office.hanseo.ac.kr")) {
        //     return ResponseEntity.badRequest().body("한서대학교 오피스 계정(@office.hanseo.ac.kr)만 사용할 수 있습니다.");
        // }

        try {
            String code = verificationService.generateAndStoreCode(email);
            emailService.sendVerificationCode(email, "회원가입 인증 코드", code);
            return ResponseEntity.ok("인증 코드가 성공적으로 발송되었습니다. 이메일을 확인해주세요.");
        } catch (Exception e) {
            e.printStackTrace(); // 에러 확인용 로그 출력
            return ResponseEntity.internalServerError().body("이메일 발송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 재설정을 위한 이메일 인증 코드를 발송하는 API 입니다.
     */
    @PostMapping("/password/verification-requests")
    public ResponseEntity<String> sendPasswordResetCode(@RequestBody EmailVerificationRequest request) {
        String email = request.getEmail();
        try {
            String code = verificationService.generateAndStoreCode(email);
            emailService.sendVerificationCode(email, "비밀번호 재설정 인증 코드", code);
            return ResponseEntity.ok("인증 코드가 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("이메일 발송 중 오류가 발생했습니다.");
        }
    }
}
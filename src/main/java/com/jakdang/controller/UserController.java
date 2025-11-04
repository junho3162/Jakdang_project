package com.jakdang.controller;

import com.jakdang.dto.*;
import com.jakdang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AddUserRequest request) {
        try {
            userService.signup(request);
            return ResponseEntity.status(201).body("회원가입이 성공적으로 완료되었습니다.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일 또는 닉네임입니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            TokenInfo tokenInfo = userService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(tokenInfo);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("로그인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/mypage")
    public ResponseEntity<UserResponse> getMyInfo(Authentication authentication) {
        String userEmail = authentication.getName();
        UserResponse userInfo = userService.getUserInfoByEmail(userEmail);
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/password/change")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * [신규 추가!]
     * 인증된 사용자의 프로필 정보(학년, 학과, 관심 태그)를 수정합니다.
     * @param request 수정할 프로필 정보가 담긴 DTO
     * @param authentication 현재 로그인한 사용자 정보
     * @return 성공 메시지 또는 오류 메시지
     */
    @PutMapping("/mypage")
    public ResponseEntity<String> updateProfile(@RequestBody ProfileUpdateRequest request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            userService.updateProfile(userEmail, request);
            return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}



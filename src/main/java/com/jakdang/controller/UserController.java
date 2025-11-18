package com.jakdang.controller;

import com.jakdang.domain.UserTag;
import com.jakdang.dto.*;
import com.jakdang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException; // 인증 실패 예외 처리를 위해 추가
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

    /**
     * 로그인 요청을 처리하는 API 입니다.
     * @param request 이메일과 비밀번호가 담긴 DTO
     * @return 성공 시, JWT 토큰 정보가 담긴 응답 / 실패 시, 오류 메시지 응답
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) { // 반환 타입을 와일드카드(?)로 변경
        try {
            // UserService의 login 메소드를 호출합니다. 이 메소드는 Exception을 던질 수 있습니다.
            TokenInfo tokenInfo = userService.login(request.getEmail(), request.getPassword());
            // 성공 시, 토큰 정보를 응답합니다.
            return ResponseEntity.ok(tokenInfo);
        } catch (AuthenticationException e) {
            // Spring Security의 공식 인증 실패 예외(비밀번호 틀림, 아이디 없음 등)를 처리합니다.
            return ResponseEntity.status(401).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            // 그 외 UserService.login()에서 발생할 수 있는 모든 예외를 처리합니다.
            return ResponseEntity.internalServerError().body("로그인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 인증된 사용자의 정보를 조회하는 마이페이지 API 입니다.
     * 이 API는 요청 헤더에 유효한 JWT 토큰이 있어야만 접근할 수 있습니다.
     * @param authentication Spring Security가 JWT 토큰을 검증한 후, 해당 사용자의 인증 정보를 담아 주입해주는 객체
     * @return 사용자의 상세 정보 (비밀번호 제외)
     */
    @GetMapping("/mypage")
    public ResponseEntity<UserResponse> getMyInfo(Authentication authentication) {
        // Authentication 객체에서 사용자의 이름(우리가 UserDetails에 설정한 email)을 가져옵니다.
        String userEmail = authentication.getName();
        // UserService를 호출하여 사용자 정보를 조회합니다.
        UserResponse userInfo = userService.getUserInfoByEmail(userEmail);

        // 조회된 사용자 정보를 HTTP 200 OK 상태와 함께 응답합니다.
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 인증 코드를 검증하고 사용자의 비밀번호를 변경하는 API 입니다.
     * @param request 이메일, 인증코드, 새 비밀번호가 담긴 DTO
     * @return 성공 메시지 또는 오류 메시지
     */
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
     * 마이페이지에서 내 관심 태그를 수정하는 API
     */
    @PutMapping("/mypage/tags")
    public ResponseEntity<String> updateMyTags(
            Authentication authentication,
            @RequestBody UpdateUserTagsRequest request
    ) {
        String userEmail = authentication.getName();
        userService.updateMyTags(userEmail, request.getTags());
        return ResponseEntity.ok("관심 태그가 성공적으로 변경되었습니다.");
    }

    /**
     * 선택 가능한 전체 태그 목록을 내려주는 API
     * (마이페이지에서 동글동글 칩 UI 만들 때 사용)
     */
    @GetMapping("/tags/options")
    public ResponseEntity<UserTag[]> getTagOptions() {
        return ResponseEntity.ok(UserTag.values());
    }
}


package com.jakdang.service;

import com.jakdang.config.jwt.JwtTokenProvider;
import com.jakdang.domain.User;
import com.jakdang.dto.AddUserRequest;
import com.jakdang.dto.PasswordChangeRequest;
import com.jakdang.dto.ProfileUpdateRequest;
import com.jakdang.dto.TokenInfo;
import com.jakdang.dto.UserResponse;
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationService verificationService;
    private final EmailService emailService; // 1. (추가) EmailService 의존성 주입

    @Transactional
    public User signup(AddUserRequest request) {
        // ... (기존 signup 메소드는 그대로) ...
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + request.getEmail());
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + request.getNickname());
        }
        if (!verificationService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }
        User user = request.toEntity(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public TokenInfo login(String email, String password) throws Exception {
        // ... (기존 login 메소드는 그대로) ...
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
        return tokenInfo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ... (기존 loadUserByUsername 메소드는 그대로) ...
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 사용하는 사용자를 찾을 수 없습니다: " + email));
    }

    public UserResponse getUserInfoByEmail(String email) {
        // ... (기존 getUserInfoByEmail 메소드는 그대로) ...
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return new UserResponse(user);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        // ... (기존 changePassword 메소드는 그대로) ...
        if (!verificationService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 사용하는 사용자를 찾을 수 없습니다: " + request.getEmail()));
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void updateProfile(String email, ProfileUpdateRequest request) {
        // ... (기존 updateProfile 메소드는 그대로) ...
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        user.updateProfile(request.getGrade(), request.getDepartment(), request.getInterestTags());
    }

    /**
     * 2. (신규 추가) 회원가입을 위한 인증 코드를 발송합니다.
     * @param email
     */
    public void sendVerificationCodeForSignup(String email) {
        // (보안 강화) 회원가입 코드 요청 시, 이미 가입된 이메일인지 확인합니다.
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + email);
        }

        String code = verificationService.generateAndStoreCode(email);
        emailService.sendVerificationCode(email, "회원가입 인증 코드", code);
    }

    /**
     * 3. (신규 추가) 비밀번호 재설정을 위한 인증 코드를 발송합니다.
     * @param email
     */
    public void sendVerificationCodeForPasswordReset(String email) {
        // (핵심!) DB에서 사용자가 존재하는지 확인합니다.
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    // 사용자가 존재할 때만! 인증 코드를 생성하고 이메일을 발송합니다.
                    String code = verificationService.generateAndStoreCode(email);
                    emailService.sendVerificationCode(email, "비밀번호 재설정 인증 코드", code);
                });

        // (중요!) 사용자가 존재하지 않아도, 이 메소드는 아무런 예외를 던지지 않습니다.
        // 이는 해커가 이메일 존재 여부를 추측하는 "사용자 열거 공격"을 방지하기 위함입니다.
    }
}


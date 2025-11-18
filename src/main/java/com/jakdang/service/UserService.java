package com.jakdang.service;

import com.jakdang.config.jwt.JwtTokenProvider;
import com.jakdang.domain.User;
import com.jakdang.domain.UserTag;
import com.jakdang.dto.AddUserRequest;
import com.jakdang.dto.PasswordChangeRequest;
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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationService verificationService;

    @Transactional
    public User signup(AddUserRequest request) {
        // (개선!) 가입 시도 자체를 막기 위해, 이메일/닉네임 중복 검사를 인증 코드 검증보다 먼저 수행합니다.
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + request.getEmail());
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + request.getNickname());
        }

        // 인증 코드가 유효한지 검증합니다.
        if (!verificationService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        User user = request.toEntity(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public TokenInfo login(String email, String password) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
        return tokenInfo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 사용하는 사용자를 찾을 수 없습니다: " + email));
    }

    public UserResponse getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return new UserResponse(user);
    }

    /**
     * 비밀번호 변경을 처리하는 메소드입니다.
     * @param request 비밀번호 변경에 필요한 정보(이메일, 인증코드, 새 비밀번호)가 담긴 DTO
     */
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        // 1. 인증 코드가 유효한지 먼저 검증합니다.
        if (!verificationService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        // 2. 이메일로 사용자를 찾습니다.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 사용하는 사용자를 찾을 수 없습니다: " + request.getEmail()));

        // 3. 새 비밀번호를 암호화하여 사용자 정보에 업데이트합니다.
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        // @Transactional 어노테이션에 의해 메소드가 끝나면 변경된 내용이 자동으로 DB에 반영됩니다.
    }

    @Transactional
    public void updateMyTags(String email, Set<UserTag> tags) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        user.updateTags(tags);
    }

}


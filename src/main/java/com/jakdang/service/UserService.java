package com.jakdang.service;

import com.jakdang.config.jwt.JwtTokenProvider;
import com.jakdang.domain.User;
import com.jakdang.dto.AddUserRequest;
import com.jakdang.dto.TokenInfo;
import com.jakdang.dto.UserResponse;
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// --- AuthenticationManager를 직접 가져오기 위해 AuthenticationConfiguration을 import 합니다. ---
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
    // --- AuthenticationManager 대신, AuthenticationManager를 얻을 수 있는 AuthenticationConfiguration을 주입받습니다. ---
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenProvider jwtTokenProvider;


    public User signup(AddUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + request.getEmail());
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + request.getNickname());
        }
        User user = request.toEntity(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public TokenInfo login(String email, String password) throws Exception { // 예외 처리를 위해 throws Exception 추가
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        // --- 주입받은 AuthenticationConfiguration을 통해 AuthenticationManager를 얻어와 인증을 수행합니다. ---
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

    /**
     * 이메일을 기반으로 사용자 정보를 조회하여 UserResponse DTO로 반환하는 메소드입니다.
     * @param email 조회할 사용자의 이메일
     * @return 변환된 사용자 정보 DTO
     */
    public UserResponse getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new UserResponse(user);
    }
}


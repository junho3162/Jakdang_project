package com.jakdang.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List; // List import 추가
import java.util.ArrayList; // ArrayList import 추가

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// (중요) 테이블 이름을 명시적으로 'users'로 지정합니다.
// MySQL에서 'user'는 예약어일 수 있어 오류를 방지하기 위함입니다.
@Table(name = "users")
public class User implements UserDetails {

    // (핵심!) 이 필드가 테이블의 Primary Key(기본 키)임을 나타냅니다.
    @Id
    // (핵심!) 기본 키 값을 자동으로 생성하는 전략을 설정합니다.
    // GenerationType.IDENTITY는 데이터베이스(MySQL)의 AUTO_INCREMENT 기능을 사용하겠다는 명확한 지시입니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "grade", nullable = false)
    private String grade;

    @Column(name = "department", nullable = false)
    private String department;

    // --- 관심 태그 필드 추가 ---
    /**
     * @ElementCollection: 이 필드가 단순한 값(String, Integer 등)의 컬렉션임을 나타냅니다.
     * JPA는 'user_interest_tags'라는 별도의 테이블을 자동으로 생성하여 이 목록을 관리합니다.
     * fetch = FetchType.LAZY: User를 조회할 때 당장 태그가 필요 없으면, 나중에 실제 사용할 때 조회하도록 설정 (성능 최적화)
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_interest_tags", joinColumns = @JoinColumn(name = "user_id")) // 연결될 테이블 정보
    @Column(name = "tag") // 태그가 저장될 컬럼 이름
    private List<String> interestTags = new ArrayList<>();

    @Builder
    public User(String email, String password, String username, String nickname, String grade, String department, List<String> interestTags) { // 빌더에 interestTags 추가
        this.email = email;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.grade = grade;
        this.department = department;
        this.interestTags = interestTags != null ? interestTags : new ArrayList<>(); // null 방지
    }

    /**
     * 새로운 암호화된 비밀번호로 현재 비밀번호를 업데이트합니다.
     * @param newPassword 암호화된 새 비밀번호
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 마이페이지에서 프로필 정보를 업데이트하는 메소드입니다.
     * @param grade 새로운 학년
     * @param department 새로운 학과
     * @param interestTags 새로운 관심 태그 목록
     */
    public void updateProfile(String grade, String department, List<String> interestTags) {
        this.grade = grade;
        this.department = department;
        this.interestTags = interestTags;
    }

    // --- UserDetails 인터페이스 구현 메소드들 (이전과 동일) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getRealUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}


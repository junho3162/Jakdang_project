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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ElementCollection(targetClass = UserTag.class)
    @CollectionTable(
            name = "user_tags",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)   // ★ enum 이름을 문자열로 저장
    @Column(name = "tag")
    private Set<UserTag> tags = new HashSet<>();

    @Builder
    public User(String email,
                String password,
                String username,
                String nickname,
                String grade,
                String department,
                Set<UserTag> tags) {

        this.email = email;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.grade = grade;
        this.department = department;
        if (tags != null) {
            this.tags = tags;
        }
    }

    /**
     * 새로운 암호화된 비밀번호로 현재 비밀번호를 업데이트합니다.
     * @param newPassword 암호화된 새 비밀번호
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
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

    // --- 태그 관련 비즈니스 메서드 ---
    public void addTag(UserTag tag) {
        if (tag == null) return;
        this.tags.add(tag);
    }

    public void addTags(Collection<UserTag> tags) {
        if (tags == null) return;
        this.tags.addAll(tags);
    }

    public void removeTag(UserTag tag) {
        if (tag == null) return;
        this.tags.remove(tag);
    }

    public void updateTags(Collection<UserTag> newTags) {
        this.tags.clear();
        if (newTags != null) {
            this.tags.addAll(newTags);
        }
    }
}


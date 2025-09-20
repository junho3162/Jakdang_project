package com.jakdang.repository;

import com.jakdang.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<관리할 Entity, Entity의 PK 타입> 를 상속받아
// Team Entity에 대한 기본적인 CRUD 메소드를 자동으로 생성합니다.
public interface TeamRepository extends JpaRepository<Team, Long> {
    // 지금은 기본 CRUD 기능만으로 충분하지만,
    // 나중에 '카테고리별 공고 조회' 같은 특정 조건의 검색이 필요할 때
    // 여기에 메소드를 추가하게 됩니다. (예: List<Team> findByCategory(String category);)
}

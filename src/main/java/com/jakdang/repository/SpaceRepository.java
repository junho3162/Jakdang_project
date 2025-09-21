package com.jakdang.repository;

import com.jakdang.domain.Space;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {
    // 향후 건물별, 층별 공간 검색을 위한 메소드 추가 가능
}

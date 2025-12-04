package com.jakdang.repository;

import com.jakdang.domain.Favorite;
import com.jakdang.domain.Team;
import com.jakdang.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    // 유저가 특정 팀을 찜했는지 확인
    boolean existsByUserAndTeam(User user, Team team);

    // 유저와 팀 정보로 즐겨찾기 내역 조회 (삭제할 때 사용)
    Optional<Favorite> findByUserAndTeam(User user, Team team);

    // 내가 찜한 목록 조회용
    List<Favorite> findAllByUser(User user);
}
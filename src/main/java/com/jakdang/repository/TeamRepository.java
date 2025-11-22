package com.jakdang.repository;

import com.jakdang.domain.Team;
import com.jakdang.domain.TeamTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

// JpaRepository<관리할 Entity, Entity의 PK 타입> 를 상속받아
// Team Entity에 대한 기본적인 CRUD 메소드를 자동으로 생성합니다.
public interface TeamRepository extends JpaRepository<Team, Long> {
    // 지금은 기본 CRUD 기능만으로 충분하지만,
    // 나중에 '카테고리별 공고 조회' 같은 특정 조건의 검색이 필요할 때
    // 여기에 메소드를 추가하게 됩니다. (예: List<Team> findByCategory(String category);)
    List<Team> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);

    @Query("SELECT t FROM Team t JOIN t.tags tag WHERE tag = :tag")
    List<Team> findByTag(@Param("tag") TeamTag tag);

    @Query("SELECT DISTINCT t FROM Team t JOIN t.tags tag WHERE tag IN :tags")
    List<Team> findByTags(@Param("tags") Set<TeamTag> tags);

    List<Team> searchByKeywordAndTags(String keyword, Set<TeamTag> tags);
}

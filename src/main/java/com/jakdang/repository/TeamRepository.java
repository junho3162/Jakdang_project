package com.jakdang.repository;

import com.jakdang.domain.Team;
import com.jakdang.domain.TeamTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 제목 또는 내용에 키워드가 포함된 팀 검색 (대소문자 구분 X)
     */
    List<Team> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleKeyword,
            String contentKeyword
    );

    /**
     * 태그만으로 검색할 때 사용 (tags 컬렉션에 전달된 태그들 중 하나라도 포함된 팀)
     */
    List<Team> findByTagsIn(Set<TeamTag> tags);

    /**
     * 키워드 + 태그 동시 검색
     * - 제목/내용에 키워드 포함
     * - tags 컬렉션에 전달된 태그들 중 하나라도 포함
     */
    @Query("""
        SELECT DISTINCT t
        FROM Team t
        JOIN t.tags tag
        WHERE 
            (LOWER(t.title)   LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR
             LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND tag IN :tags
        """)
    List<Team> searchByKeywordAndTags(
            @Param("keyword") String keyword,
            @Param("tags") Set<TeamTag> tags
    );
}

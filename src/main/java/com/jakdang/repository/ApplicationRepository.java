package com.jakdang.repository;

import com.jakdang.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository<관리할 Entity, Entity의 PK 타입> 를 상속받아
// Application Entity에 대한 기본적인 CRUD 메소드를 자동으로 생성합니다.
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    /**
     * 특정 팀(Team)에 속한 모든 지원서(Application) 목록을 조회합니다.
     * Spring Data JPA의 쿼리 메소드 규칙(findBy + {조회할 필드 이름})에 따라
     * Spring이 이 메소드 이름만 보고도 알아서 SQL 쿼리를 생성해줍니다.
     * @param teamId 조회할 팀의 ID
     * @return 해당 팀의 지원서 리스트
     */
    List<Application> findByTeamId(Long teamId);
}

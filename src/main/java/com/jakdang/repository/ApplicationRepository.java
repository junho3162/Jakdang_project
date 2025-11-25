package com.jakdang.repository;

import com.jakdang.domain.Application;
import com.jakdang.domain.User; // 1. (추가) User 엔티티 import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // 2. (추가) @Repository 어노테이션
import java.util.List;

@Repository // 3. (추가) @Repository 어노테이션
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * 특정 팀(Team)에 속한 모든 지원서(Application) 목록을 조회합니다.
     * (팀장용)
     */
    List<Application> findByTeamId(Long teamId);

    /**
     * (신규 추가!)
     * 특정 사용자(Applicant)가 지원한 모든 지원서 목록을 조회합니다.
     * (지원자 본인의 '내 지원 현황'용)
     */
    List<Application> findAllByApplicant(User applicant);
}
package com.jakdang.repository;

import com.jakdang.domain.Application;
import com.jakdang.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * 특정 팀(Team)에 속한 모든 지원서(Application) 목록을 조회합니다.
     * (팀장용)
     */
    List<Application> findByTeamId(Long teamId);

    /**
     * 특정 지원자(Applicant)가 지원한 모든 지원서 목록을 조회합니다.
     * (지원자 본인의 '내 지원 현황'용)
     */
    List<Application> findAllByApplicant(User applicant);
}
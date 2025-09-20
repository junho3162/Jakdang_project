package com.jakdang.service;

import com.jakdang.domain.Application;
import com.jakdang.domain.Team;
import com.jakdang.domain.User;
import com.jakdang.dto.ApplicationResponse;
import com.jakdang.dto.ApplyTeamRequest;
import com.jakdang.repository.ApplicationRepository;
import com.jakdang.repository.TeamRepository;
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 팀 지원 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    /**
     * 특정 팀에 지원서를 제출하는 메소드입니다.
     * @param teamId 지원할 팀의 ID
     * @param request 지원서 내용(메시지)
     * @param applicantEmail 지원자의 이메일
     * @return 생성된 Application 엔티티
     */
    @Transactional
    public Application applyToTeam(Long teamId, ApplyTeamRequest request, String applicantEmail) {
        // 지원자 정보를 DB에서 조회합니다.
        User applicant = userRepository.findByEmail(applicantEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + applicantEmail));

        // 지원할 팀 정보를 DB에서 조회합니다.
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        // 자신이 팀장인 팀에는 지원할 수 없습니다.
        if (Objects.equals(team.getLeader().getId(), applicant.getId())) {
            throw new IllegalArgumentException("자신이 팀장인 팀에는 지원할 수 없습니다.");
        }

        // TODO: 이미 지원한 팀에 중복 지원하는 것을 방지하는 로직 추가 필요

        // Application 엔티티를 생성합니다.
        Application application = Application.builder()
                .applicant(applicant)
                .team(team)
                .message(request.getMessage())
                .build();

        // 생성된 지원서를 DB에 저장합니다.
        return applicationRepository.save(application);
    }

    /**
     * 특정 팀의 모든 지원서 목록을 조회하는 메소드입니다.
     * @param teamId 조회할 팀의 ID
     * @param userEmail 요청을 보낸 사용자의 이메일 (권한 확인용)
     * @return 해당 팀의 지원서 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> findApplicationsByTeam(Long teamId, String userEmail) {
        // 먼저 팀 정보를 조회합니다.
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        // (핵심!) 현재 로그인한 사용자가 조회하려는 팀의 팀장이 맞는지 확인합니다.
        if (!Objects.equals(team.getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("지원자 목록을 조회할 권한이 없습니다.");
        }

        // ApplicationRepository를 사용해 해당 팀의 모든 지원서를 가져옵니다.
        List<Application> applications = applicationRepository.findByTeamId(teamId);

        // 가져온 Application 엔티티 리스트를 ApplicationResponse DTO 리스트로 변환하여 반환합니다.
        return applications.stream()
                .map(ApplicationResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 지원서를 수락하거나 거절하는 메소드입니다.
     * @param applicationId 처리할 지원서의 ID
     * @param newStatus 새로운 상태 ("ACCEPTED" 또는 "REJECTED")
     * @param userEmail 요청을 보낸 사용자의 이메일 (권한 확인용)
     * @return 상태가 변경된 지원서 정보 DTO
     */
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, String newStatus, String userEmail) {
        // 지원서 정보를 조회합니다.
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 지원서를 찾을 수 없습니다: " + applicationId));

        // 요청을 보낸 사용자가 해당 지원서가 속한 팀의 팀장이 맞는지 확인합니다.
        if (!Objects.equals(application.getTeam().getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("지원서를 처리할 권한이 없습니다.");
        }

        // 지원서 상태를 업데이트합니다.
        application.updateStatus(newStatus);

        // TODO: 수락("ACCEPTED") 시, Team의 멤버 목록에 지원자를 추가하는 로직 필요

        return new ApplicationResponse(application);
    }
}

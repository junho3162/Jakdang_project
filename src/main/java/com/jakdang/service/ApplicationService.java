package com.jakdang.service;

import com.jakdang.domain.Application;
import com.jakdang.domain.Team;
import com.jakdang.domain.User;
import com.jakdang.dto.ApplicationResponse;
import com.jakdang.dto.MyApplicationResponse;
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
     *
     * @param teamId         지원할 팀의 ID
     * @param request        지원서 내용(메시지)
     * @param applicantEmail 지원자의 이메일
     * @return 생성된 Application 엔티티
     */
    @Transactional
    public Application applyToTeam(Long teamId, ApplyTeamRequest request, String applicantEmail) {
        // 1. 지원할 팀을 조회합니다.
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        // 2. 지원자를 이메일로 조회합니다.
        User applicant = userRepository.findByEmail(applicantEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + applicantEmail));

        // 3. Application 엔티티를 생성합니다.
        Application application = Application.builder()
                .team(team)
                .applicant(applicant)
                .message(request.getMessage())
                .status("PENDING") // 기본 상태를 'PENDING(대기중)'으로 설정
                .build();

        // 4. DB에 저장합니다.
        return applicationRepository.save(application);
    }

    /**
     * 특정 팀에 대한 모든 지원서를 조회하는 메소드입니다.
     *
     * @param teamId 조회할 팀의 ID
     * @return 해당 팀에 지원한 모든 지원서 목록
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsForTeam(Long teamId, String leaderEmail) {
        // 1. 팀을 조회합니다.
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        // 2. 요청을 보낸 사용자가 이 팀의 팀장인지 확인합니다.
        if (!Objects.equals(team.getLeader().getEmail(), leaderEmail)) {
            throw new AccessDeniedException("이 팀의 지원서를 볼 권한이 없습니다.");
        }

        // 3. 팀 ID로 모든 지원서를 조회합니다.
        List<Application> applications = applicationRepository.findByTeamId(teamId);

        // 4. 조회된 지원서를 DTO로 변환하여 반환합니다.
        return applications.stream()
                .map(ApplicationResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 팀장이 지원서의 상태를 변경하는 메소드입니다.
     *
     * @param applicationId 상태를 변경할 지원서 ID
     * @param newStatus     새로운 상태 값 (예: "ACCEPTED", "REJECTED")
     * @param userEmail     요청을 보낸 사용자의 이메일
     * @return 상태가 변경된 ApplicationResponse DTO
     */
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, String newStatus, String userEmail) {
        // 1. 지원서를 ID로 조회합니다.
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 지원서를 찾을 수 없습니다: " + applicationId));

        // 2. 요청을 보낸 사용자가 이 지원서가 속한 팀의 팀장인지 확인합니다.
        if (!Objects.equals(application.getTeam().getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("지원서를 처리할 권한이 없습니다.");
        }

        // 3. 지원서 상태를 업데이트합니다.
        application.updateStatus(newStatus);

        // TODO: 수락("ACCEPTED") 시, Team의 멤버 목록에 지원자를 추가하는 로직 필요

        // 4. 변경된 지원서를 DTO로 감싸서 반환합니다.
        return new ApplicationResponse(application);
    }

    /**
     * (추가) 내가 지원한 팀들의 지원 현황을 조회하는 메소드입니다.
     *
     * @param applicantEmail 현재 로그인한 사용자 이메일
     * @return MyApplicationResponse DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MyApplicationResponse> findMyApplications(String applicantEmail) {
        // 1. 이메일로 User 객체를 찾습니다.
        User applicant = userRepository.findByEmail(applicantEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + applicantEmail));

        // 2. Repository에서 해당 사용자가 지원한 모든 지원서를 찾습니다.
        List<Application> applications = applicationRepository.findAllByApplicant(applicant);

        // 3. 각 지원서(Application)를 MyApplicationResponse DTO로 변환하여 리스트로 반환합니다.
        return applications.stream()
                .map(MyApplicationResponse::new)
                .collect(Collectors.toList());
    }
}
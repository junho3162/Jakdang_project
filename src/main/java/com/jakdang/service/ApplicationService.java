package com.jakdang.service;

import com.jakdang.domain.Application;
import com.jakdang.domain.Team;
import com.jakdang.domain.User;
import com.jakdang.dto.ApplyTeamRequest;
import com.jakdang.dto.ApplicationResponse;
import com.jakdang.dto.MyApplicationResponse; // 1. (추가) '내 지원 현황' DTO import
import com.jakdang.repository.ApplicationRepository;
import com.jakdang.repository.TeamRepository;
import com.jakdang.repository.UserRepository; // 2. (추가) UserRepository import
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 3. (추가) 예외 import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors; // 4. (추가) Collectors import

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository; // 5. (추가) UserRepository 의존성 주입

    /**
     * 특정 팀에 지원하는 로직
     */
    @Transactional
    public void applyToTeam(Long teamId, ApplyTeamRequest request, String applicantEmail) {
        User applicant = userRepository.findByEmail(applicantEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + applicantEmail));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        // (예외 처리) 팀장이 본인 팀에 지원하는 것 방지
        if (Objects.equals(team.getLeader().getId(), applicant.getId())) {
            throw new IllegalArgumentException("팀장은 본인의 공고에 지원할 수 없습니다.");
        }

        Application application = Application.builder()
                .applicant(applicant)
                .team(team)
                .message(request.getMessage())
                .status("대기중") // 지원 시 기본 상태
                .build();

        applicationRepository.save(application);
    }

    /**
     * 특정 팀의 지원자 목록을 조회하는 로직 (팀장용)
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> findApplicationsByTeam(Long teamId, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        // (권한 확인) 현재 로그인한 사용자가 팀장인지 확인
        if (!Objects.equals(team.getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("지원자 목록을 조회할 권한이 없습니다.");
        }

        List<Application> applications = applicationRepository.findByTeamId(teamId);

        return applications.stream()
                .map(ApplicationResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 지원서 상태를 변경하는 로직 (팀장용)
     */
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, String newStatus, String userEmail) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 지원서를 찾을 수 없습니다: " + applicationId));

        // (권한 확인) 현재 로그인한 사용자가 팀장인지 확인
        if (!Objects.equals(application.getTeam().getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("지원 상태를 변경할 권한이 없습니다.");
        }

        application.updateStatus(newStatus);

        return new ApplicationResponse(application);
    }

    /**
     * (신규 추가!)
     * 특정 사용자가 지원한 모든 지원 내역을 조회합니다. (지원자 본인용)
     * @param applicantEmail 현재 로그인한 사용자의 이메일
     * @return MyApplicationResponse DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MyApplicationResponse> findMyApplications(String applicantEmail) {
        // 1. 이메일로 User 객체를 찾습니다.
        User applicant = userRepository.findByEmail(applicantEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + applicantEmail));

        // 2. Repository에서 해당 사용자가 지원한 모든 지원서를 찾습니다.
        List<Application> applications = applicationRepository.findAllByApplicant(applicant);

        // 3. (핵심) 각 지원서(Application)를 MyApplicationResponse DTO로 변환하여 리스트로 반환합니다.
        return applications.stream()
                .map(MyApplicationResponse::new)
                .collect(Collectors.toList());
    }
}
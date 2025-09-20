package com.jakdang.controller;

import com.jakdang.domain.Team;
import com.jakdang.dto.*;
import com.jakdang.service.ApplicationService;
import com.jakdang.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final ApplicationService applicationService; // ApplicationService 의존성 주입

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@RequestBody CreateTeamRequest request, Authentication authentication) {
        String leaderEmail = authentication.getName();
        Team createdTeam = teamService.createTeam(request, leaderEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TeamResponse(createdTeam));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        List<TeamResponse> teams = teamService.findAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long teamId) {
        TeamResponse teamInfo = teamService.findTeamById(teamId);
        return ResponseEntity.ok(teamInfo);
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<?> updateTeam(@PathVariable Long teamId, @RequestBody UpdateTeamRequest request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            TeamResponse updatedTeam = teamService.updateTeam(teamId, request, userEmail);
            return ResponseEntity.ok(updatedTeam);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long teamId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            teamService.deleteTeam(teamId, userEmail);
            return ResponseEntity.ok(teamId + "번 공고가 성공적으로 삭제되었습니다.");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * 특정 팀에 지원하는 API 입니다.
     * @param teamId 지원할 팀의 ID
     * @param request 지원 메시지가 담긴 DTO
     * @param authentication 현재 로그인한 사용자 정보
     * @return 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/{teamId}/apply")
    public ResponseEntity<String> applyToTeam(@PathVariable Long teamId, @RequestBody ApplyTeamRequest request, Authentication authentication) {
        try {
            String applicantEmail = authentication.getName();
            applicationService.applyToTeam(teamId, request, applicantEmail);
            return ResponseEntity.ok("팀 지원이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            // Service에서 발생시킨 비즈니스 규칙 위반(팀장 지원, 팀 없음 등) 또는 사용자 없음 예외를 처리합니다.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 그 외 예상치 못한 서버 내부 오류를 처리합니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("지원 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 특정 팀의 지원자 목록을 조회하는 API 입니다. (팀장 전용)
     * @param teamId 조회할 팀의 ID
     * @param authentication 현재 로그인한 사용자 정보
     * @return 해당 팀의 지원자 리스트
     */
    @GetMapping("/{teamId}/applications")
    public ResponseEntity<?> getApplicationsByTeam(@PathVariable Long teamId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ApplicationResponse> applications = applicationService.findApplicationsByTeam(teamId, userEmail);
            return ResponseEntity.ok(applications);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * 특정 지원서의 상태를 변경(수락/거절)하는 API 입니다. (팀장 전용)
     * @param teamId 팀 ID (URL 경로 일관성을 위해 포함)
     * @param applicationId 처리할 지원서 ID
     * @param payload 새로운 상태 정보 (예: {"status": "ACCEPTED"})
     * @param authentication 현재 로그인한 사용자 정보
     * @return 상태가 변경된 지원서 정보
     */
    @PatchMapping("/{teamId}/applications/{applicationId}")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long teamId,
                                                     @PathVariable Long applicationId,
                                                     @RequestBody Map<String, String> payload,
                                                     Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            String newStatus = payload.get("status");

            // 상태 값이 제대로 전달되었는지, 그리고 유효한 값인지 확인합니다.
            if (newStatus == null || (!newStatus.equals("ACCEPTED") && !newStatus.equals("REJECTED"))) {
                return ResponseEntity.badRequest().body("잘못된 상태 값입니다. 'ACCEPTED' 또는 'REJECTED'만 가능합니다.");
            }

            ApplicationResponse updatedApplication = applicationService.updateApplicationStatus(applicationId, newStatus, userEmail);
            return ResponseEntity.ok(updatedApplication);
        } catch (AccessDeniedException e) {
            // Service에서 권한 없음 예외 발생 시, 403 Forbidden 응답
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Service에서 해당 ID를 찾지 못했을 때, 400 Bad Request 응답
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}


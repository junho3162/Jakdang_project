package com.jakdang.controller;

import com.jakdang.domain.Team;
import com.jakdang.dto.ApplyTeamRequest;
import com.jakdang.dto.ApplicationResponse;
import com.jakdang.dto.CreateTeamRequest;
import com.jakdang.dto.TeamResponse;
import com.jakdang.dto.UpdateTeamRequest;
import com.jakdang.service.ApplicationService;
import com.jakdang.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // (추가!)
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // (추가!)

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams") // 이 컨트롤러의 모든 API는 "/api/teams"로 시작
public class TeamController {

    private final TeamService teamService;
    private final ApplicationService applicationService; // 팀 지원/관리를 위해 주입

    /**
     * (대폭 수정!)
     * 새로운 모집 공고를 생성하는 API 입니다. (파일 업로드 포함)
     * consumes = MediaType.MULTIPART_FORM_DATA_VALUE -> 이제 JSON이 아닌 FormData를 받습니다.
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<TeamResponse> createTeam(
            @RequestPart("requestDto") CreateTeamRequest request, // 1. JSON 데이터
            @RequestPart(value = "file", required = false) MultipartFile file, // 2. (선택적) 파일 데이터
            Authentication authentication) {

        String leaderEmail = authentication.getName();

        // 3. Service에 DTO와 file을 함께 전달
        Team createdTeam = teamService.createTeam(request, file, leaderEmail);

        return ResponseEntity.status(HttpStatus.CREATED).body(new TeamResponse(createdTeam));
    }

    /**
     * 모든 모집 공고 목록을 조회하는 API 입니다.
     */
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        List<TeamResponse> teams = teamService.findAllTeams();
        return ResponseEntity.ok(teams);
    }

    /**
     * 특정 ID의 모집 공고 상세 정보를 조회하는 API 입니다.
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long teamId) {
        TeamResponse teamInfo = teamService.findTeamById(teamId);
        return ResponseEntity.ok(teamInfo);
    }

    /**
     * (수정!) 특정 ID의 모집 공고를 수정하는 API 입니다. (파일 수정은 일단 제외)
     * (참고) 파일 수정을 원하면 이 API도 Post처럼 Multipart로 변경해야 합니다.
     */
    @PutMapping("/{teamId}")
    public ResponseEntity<?> updateTeam(@PathVariable Long teamId,
                                        @RequestBody UpdateTeamRequest request,
                                        Authentication authentication) {
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

    /**
     * 특정 ID의 모집 공고를 삭제하는 API 입니다.
     */
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

    // --- 팀 지원 및 관리 API ---

    /**
     * 특정 팀에 지원하는 API 입니다.
     */
    @PostMapping("/{teamId}/apply")
    public ResponseEntity<String> applyToTeam(@PathVariable Long teamId, @RequestBody ApplyTeamRequest request, Authentication authentication) {
        try {
            String applicantEmail = authentication.getName();
            applicationService.applyToTeam(teamId, request, applicantEmail);
            return ResponseEntity.ok("팀 지원이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 특정 팀의 지원자 목록을 조회하는 API 입니다. (팀장 전용)
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
     */
    @PatchMapping("/{teamId}/applications/{applicationId}")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long teamId,
                                                     @PathVariable Long applicationId,
                                                     @RequestBody Map<String, String> payload,
                                                     Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            String newStatus = payload.get("status");

            if (newStatus == null || (!newStatus.equals("ACCEPTED") && !newStatus.equals("REJECTED"))) {
                return ResponseEntity.badRequest().body("잘못된 상태 값입니다. 'ACCEPTED' 또는 'REJECTED'만 가능합니다.");
            }

            ApplicationResponse updatedApplication = applicationService.updateApplicationStatus(applicationId, newStatus, userEmail);
            return ResponseEntity.ok(updatedApplication);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}


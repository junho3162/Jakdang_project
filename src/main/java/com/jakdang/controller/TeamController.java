package com.jakdang.controller;

import com.jakdang.domain.Team;
import com.jakdang.domain.TeamTag;
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
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final ApplicationService applicationService;

    /**
     * 팀 생성 API
     * POST /api/teams
     */
    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@RequestBody CreateTeamRequest request, Authentication authentication) {
        String leaderEmail = authentication.getName();
        Team createdTeam = teamService.createTeam(request, leaderEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TeamResponse(createdTeam));
    }

    /**
     * 모든 팀 조회 API (수정됨: 로그인 유저의 즐겨찾기 여부 확인)
     * GET /api/teams
     */
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams(Authentication authentication) {
        // 인증 정보가 없으면 null, 있으면 이메일 추출
        String userEmail = (authentication != null) ? authentication.getName() : null;
        List<TeamResponse> teams = teamService.findAllTeams(userEmail);
        return ResponseEntity.ok(teams);
    }

    /**
     * 특정 팀 상세 조회 API (수정됨: 로그인 유저의 즐겨찾기 여부 확인)
     * GET /api/teams/{teamId}
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long teamId, Authentication authentication) {
        String userEmail = (authentication != null) ? authentication.getName() : null;
        TeamResponse teamInfo = teamService.findTeamById(teamId, userEmail);
        return ResponseEntity.ok(teamInfo);
    }

    /**
     * 특정 팀 검색 기능 API (수정됨: 로그인 유저의 즐겨찾기 여부 확인)
     * GET /api/teams/search
     */
    @GetMapping("/search")
    public ResponseEntity<List<TeamResponse>> searchTeams(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Set<TeamTag> tags,
            Authentication authentication
    ) {
        String userEmail = (authentication != null) ? authentication.getName() : null;
        List<TeamResponse> result = teamService.searchTeams(keyword, tags, userEmail);
        return ResponseEntity.ok(result);
    }

    /**
     * (신규 기능) 즐겨찾기 토글 API
     * POST /api/teams/{teamId}/favorite
     * Header에 JWT 토큰 필수
     */
    @PostMapping("/{teamId}/favorite")
    public ResponseEntity<String> toggleFavorite(@PathVariable Long teamId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            boolean isFavorited = teamService.toggleFavorite(teamId, userEmail);

            if (isFavorited) {
                return ResponseEntity.ok("찜 목록에 추가되었습니다.");
            } else {
                return ResponseEntity.ok("찜 목록에서 삭제되었습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 팀 수정 API
     * PUT /api/teams/{teamId}
     */
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

    /**
     * 팀 삭제 API
     * DELETE /api/teams/{teamId}
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

    /**
     * 특정 팀에 지원하는 API
     */
    @PostMapping("/{teamId}/apply")
    public ResponseEntity<String> applyToTeam(@PathVariable Long teamId, @RequestBody ApplyTeamRequest request, Authentication authentication) {
        try {
            String applicantEmail = authentication.getName();
            applicationService.applyToTeam(teamId, request, applicantEmail);
            return ResponseEntity.ok("팀 지원이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("지원 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 특정 팀의 지원자 목록을 조회하는 API (팀장 전용)
     */
    @GetMapping("/{teamId}/applications")
    public ResponseEntity<?> getApplicationsByTeam(@PathVariable Long teamId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            // ApplicationService 메소드 이름을 업로드된 파일 기준(findApplicationsByTeam)으로 사용합니다.
            List<ApplicationResponse> applications = applicationService.findApplicationsByTeam(teamId, userEmail);
            return ResponseEntity.ok(applications);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * 특정 지원서의 상태를 변경하는 API (팀장 전용)
     */
    @PatchMapping("/{teamId}/applications/{applicationId}")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long teamId,
                                                     @PathVariable Long applicationId,
                                                     @RequestBody Map<String, String> payload,
                                                     Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            String newStatus = payload.get("status");

            if (newStatus == null ||
                    !(newStatus.equals("ACCEPTED") || newStatus.equals("REJECTED"))) {
                return ResponseEntity.badRequest()
                        .body("잘못된 상태 값입니다. 'ACCEPTED' 또는 'REJECTED'만 가능합니다.");
            }

            ApplicationResponse updatedApplication =
                    applicationService.updateApplicationStatus(applicationId, newStatus, userEmail);

            return ResponseEntity.ok(updatedApplication);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
package com.jakdang.service;

import com.jakdang.domain.Team;
import com.jakdang.domain.TeamTag;
import com.jakdang.domain.User;
import com.jakdang.dto.CreateTeamRequest;
import com.jakdang.dto.TeamResponse;
import com.jakdang.dto.UpdateTeamRequest;
import com.jakdang.repository.TeamRepository;
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
// (핵심!) java.nio.file.AccessDeniedException 대신, Spring Security의 것을 사용하도록 수정합니다.
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 모집 공고(팀) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    /**
     * 새로운 모집 공고를 생성하는 메소드입니다.
     */
    @Transactional
    public Team createTeam(CreateTeamRequest request, String leaderEmail) {
        User leader = userRepository.findByEmail(leaderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + leaderEmail));

        Team newTeam = request.toEntity(leader);
        return teamRepository.save(newTeam);
    }

    /**
     * 모든 모집 공고 목록을 조회하는 메소드입니다.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> findAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(TeamResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 모든 모집 공고 목록에서 검색하는 메소드입니다.
     */
    public List<TeamResponse> searchTeams(String keyword, Set<TeamTag> tags) {
        List<Team> teams;

        if (keyword != null && tags != null) {
            teams = teamRepository.searchByKeywordAndTags(keyword, tags);
        } else if (keyword != null) {
            teams = teamRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
        } else if (tags != null) {
            teams = teamRepository.findByTagsIn(tags);
        } else {
            teams = teamRepository.findAll();
        }

        return teams.stream().map(TeamResponse::new).toList();
    }



    /**
     * ID를 기반으로 특정 모집 공고의 상세 정보를 조회하는 메소드입니다.
     */
    @Transactional(readOnly = true)
    public TeamResponse findTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));
        return new TeamResponse(team);
    }

    /**
     * 특정 모집 공고를 수정하는 메소드입니다.
     * @param teamId 수정할 공고의 ID
     * @param request 수정할 내용이 담긴 DTO
     * @param userEmail 요청을 보낸 사용자의 이메일 (권한 확인용)
     * @return 수정된 공고 정보 DTO
     */
    @Transactional
    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        if (!Objects.equals(team.getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("공고를 수정할 권한이 없습니다.");
        }

        team.update(
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getStatus(),
                request.getMaxMembers(),
                request.getDeadline(),
                request.getTags(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRecruitRoles()
        );

        return new TeamResponse(team);
    }


    /**
     * 특정 모집 공고를 삭제하는 메소드입니다.
     * @param teamId 삭제할 공고의 ID
     * @param userEmail 요청을 보낸 사용자의 이메일 (권한 확인용)
     */
    @Transactional
    public void deleteTeam(Long teamId, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        // (핵심!) 현재 로그인한 사용자가 공고의 작성자(팀장)가 맞는지 확인합니다.
        if (!Objects.equals(team.getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("공고를 삭제할 권한이 없습니다.");
        }

        teamRepository.delete(team);
    }
}


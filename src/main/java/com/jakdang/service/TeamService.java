package com.jakdang.service;

import com.jakdang.domain.Favorite; // 추가됨
import com.jakdang.domain.Team;
import com.jakdang.domain.TeamTag;
import com.jakdang.domain.User;
import com.jakdang.dto.CreateTeamRequest;
import com.jakdang.dto.TeamResponse;
import com.jakdang.dto.UpdateTeamRequest;
import com.jakdang.repository.FavoriteRepository; // 추가됨
import com.jakdang.repository.TeamRepository;
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    private final FavoriteRepository favoriteRepository; // (핵심!) 즐겨찾기 기능을 위해 추가

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
     * (수정됨) 로그인한 사용자의 경우 즐겨찾기 여부를 확인하여 설정합니다.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> findAllTeams(String userEmail) {
        List<Team> teams = teamRepository.findAll();

        // 1. 엔티티를 DTO 리스트로 변환
        List<TeamResponse> responses = teams.stream()
                .map(TeamResponse::new)
                .collect(Collectors.toList());

        // 2. 로그인 사용자라면 즐겨찾기 여부 체크
        checkFavorites(responses, userEmail);

        return responses;
    }

    /**
     * 모든 모집 공고 목록에서 검색하는 메소드입니다.
     * (수정됨) 검색 결과에도 즐겨찾기 여부를 포함하기 위해 userEmail 파라미터를 추가했습니다.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> searchTeams(String keyword, Set<TeamTag> tags, String userEmail) {
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

        // 1. 엔티티를 DTO 리스트로 변환
        List<TeamResponse> responses = teams.stream()
                .map(TeamResponse::new)
                .collect(Collectors.toList());

        // 2. 로그인 사용자라면 즐겨찾기 여부 체크
        checkFavorites(responses, userEmail);

        return responses;
    }

    /**
     * ID를 기반으로 특정 모집 공고의 상세 정보를 조회하는 메소드입니다.
     * (수정됨) 상세 조회 시에도 즐겨찾기 여부를 확인합니다.
     */
    @Transactional(readOnly = true)
    public TeamResponse findTeamById(Long teamId, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        TeamResponse response = new TeamResponse(team);

        // 로그인한 사용자이고, 해당 팀을 찜했다면 isFavorite = true 설정
        if (userEmail != null && !userEmail.equals("anonymousUser")) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null && favoriteRepository.existsByUserAndTeam(user, team)) {
                response.setFavorite(true);
            }
        }

        return response;
    }

    /**
     * (신규 추가) 즐겨찾기(찜) 기능을 토글(Toggle)하는 메소드입니다.
     * 이미 찜했다면 취소하고, 찜하지 않았다면 찜합니다.
     */
    @Transactional
    public boolean toggleFavorite(Long teamId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀을 찾을 수 없습니다."));

        // 이미 즐겨찾기 되어있는지 확인 후 토글 처리
        return favoriteRepository.findByUserAndTeam(user, team)
                .map(favorite -> {
                    favoriteRepository.delete(favorite); // 있으면 삭제 (찜 취소)
                    return false; // 결과: 찜 해제됨
                })
                .orElseGet(() -> {
                    favoriteRepository.save(new Favorite(user, team)); // 없으면 생성 (찜 하기)
                    return true; // 결과: 찜 설정됨
                });
    }

    /**
     * 특정 모집 공고를 수정하는 메소드입니다.
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
     */
    @Transactional
    public void deleteTeam(Long teamId, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        if (!Objects.equals(team.getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("공고를 삭제할 권한이 없습니다.");
        }

        teamRepository.delete(team);
    }

    // --- 내부 헬퍼 메소드 ---

    /**
     * DTO 리스트에 대해 현재 로그인한 유저가 찜한 항목인지 체크하여 마킹하는 메소드
     */
    private void checkFavorites(List<TeamResponse> responses, String userEmail) {
        if (userEmail != null && !userEmail.equals("anonymousUser")) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

            // 쿼리 최적화를 위해 내가 찜한 팀 ID 목록을 한 번에 가져옴
            Set<Long> favoriteTeamIds = favoriteRepository.findAllByUser(user).stream()
                    .map(f -> f.getTeam().getId())
                    .collect(Collectors.toSet());

            // 각 DTO를 순회하며 ID가 찜 목록에 있으면 isFavorite = true 설정
            for (TeamResponse response : responses) {
                if (favoriteTeamIds.contains(response.getId())) {
                    response.setFavorite(true);
                }
            }
        }
    }
}
package com.jakdang.service;

import com.jakdang.domain.Team;
import com.jakdang.domain.User;
import com.jakdang.dto.CreateTeamRequest;
import com.jakdang.dto.TeamResponse;
import com.jakdang.dto.UpdateTeamRequest;
import com.jakdang.repository.TeamRepository;
import com.jakdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // 1. (추가) 파일 타입을 import

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final FileService fileService; // 2. (추가) FileService 의존성 주입

    /**
     * 3. (수정) 새로운 모집 공고를 생성하는 메소드 (파일 업로드 포함)
     */
    @Transactional
    public Team createTeam(CreateTeamRequest request, MultipartFile file, String leaderEmail) {
        User leader = userRepository.findByEmail(leaderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + leaderEmail));

        // 4. FileService를 사용해 파일을 업로드하고, 저장된 URL을 받음
        String fileUrl = fileService.uploadFile(file);

        // 5. toEntity 메소드에 fileUrl을 함께 전달하여 Team 객체 생성
        Team newTeam = request.toEntity(leader, fileUrl);
        return teamRepository.save(newTeam);
    }

    /**
     * 모든 모집 공고 목록을 조회하는 메소드입니다.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> findAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(TeamResponse::new) // 6. (수정) TeamResponse 생성자로 변경 (아래 DTO 참고)
                .collect(Collectors.toList());
    }

    /**
     * ID를 기반으로 특정 모집 공고의 상세 정보를 조회하는 메소드입니다.
     */
    @Transactional(readOnly = true)
    public TeamResponse findTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));
        return new TeamResponse(team); // 7. (수정) TeamResponse 생성자로 변경
    }

    /**
     * 8. (수정) 특정 모집 공고를 수정하는 메소드 (신규 필드 반영)
     */
    @Transactional
    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 팀을 찾을 수 없습니다: " + teamId));

        if (!Objects.equals(team.getLeader().getEmail(), userEmail)) {
            throw new AccessDeniedException("공고를 수정할 권한이 없습니다.");
        }

        // 9. (수정) Entity의 update 메소드에 신규 필드 전달
        team.update(request.getTitle(), request.getContent(), request.getCategory(),
                request.getStatus(), request.getMaxMembers(), request.getDeadline(),
                request.getRequiredRoles());

        return new TeamResponse(team);
    }

    /**
     * 특정 모집 공고를 삭제하는 메소입니다.
     * (TODO: 파일도 함께 삭제하는 로직 추가 필요)
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
}


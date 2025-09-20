package com.jakdang.dto;

import com.jakdang.domain.Application;
import lombok.Getter;

/**
 * 팀 지원서 정보를 클라이언트에게 응답할 때 사용하는 DTO 입니다.
 */
@Getter
public class ApplicationResponse {

    private final Long applicationId;
    private final String applicantNickname;
    private final String applicantDepartment;
    private final String message;
    private final String status;

    // Application 엔티티를 파라미터로 받아, ApplicationResponse DTO로 변환하는 생성자입니다.
    public ApplicationResponse(Application application) {
        this.applicationId = application.getId();
        this.applicantNickname = application.getApplicant().getNickname();
        this.applicantDepartment = application.getApplicant().getDepartment();
        this.message = application.getMessage();
        this.status = application.getStatus();
    }
}

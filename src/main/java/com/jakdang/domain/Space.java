package com.jakdang.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예약 가능한 공간 정보를 담는 Entity 클래스입니다.
 * 데이터베이스의 'spaces' 테이블과 직접 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "spaces")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "space_id")
    private Long id;

    @Column(name = "space_name", nullable = false)
    private String spaceName; // 예: "인문관 301호", "학생회관 세미나실 A"

    @Column(name = "location", nullable = false)
    private String location; // 예: "인문관 3층"

    @Column(name = "capacity", nullable = false)
    private int capacity; // 수용 인원

    @Column(name = "space_type", nullable = false)
    private String spaceType; // 공간 유형 (예: "강의실", "세미나실")

    // TODO: 화이트보드, 빔 프로젝터 등 시설 정보를 담을 필드 추가 필요

    @Builder
    public Space(String spaceName, String location, int capacity, String spaceType) {
        this.spaceName = spaceName;
        this.location = location;
        this.capacity = capacity;
        this.spaceType = spaceType;
    }
}

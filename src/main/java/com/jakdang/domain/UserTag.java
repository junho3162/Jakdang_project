package com.jakdang.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTag {

    // 성격 (Personality)
    KINDNESS("친절함", TagCategory.PERSONALITY),
    LIVELY("활발함", TagCategory.PERSONALITY),
    HUMOR("유머감", TagCategory.PERSONALITY),
    CARING("배려심", TagCategory.PERSONALITY),
    SOCIABLE("사교성", TagCategory.PERSONALITY),
    PATIENCE("인내심", TagCategory.PERSONALITY),

    // 능력 (Ability)
    PROBLEM_SOLVING("문제 해결", TagCategory.ABILITY),
    FAST_PROCESSING("신속 처리", TagCategory.ABILITY),
    CREATIVITY("창의 발상", TagCategory.ABILITY),
    LOGICAL("논리 정연", TagCategory.ABILITY),
    EXECUTION("실행 능력", TagCategory.ABILITY),
    CAREER_MINDED("경력직", TagCategory.ABILITY),
    COLLABORATION("협업 능력", TagCategory.ABILITY),
    EFFICIENCY("효율 추구", TagCategory.ABILITY),
    PRESENTATION("발표 능력", TagCategory.ABILITY),
    LEARNING("학습 능력", TagCategory.ABILITY),

    // 태도 (Attitude)
    ACTIVE_PARTICIPATION("적극 참여", TagCategory.ATTITUDE),
    TAKING_INITIATIVE("솔선 수범", TagCategory.ATTITUDE),
    POSITIVE_THINKING("긍정 사고", TagCategory.ATTITUDE),
    LEARNING_PASSION("배움 열정", TagCategory.ATTITUDE),
    DILIGENCE("성실 노력", TagCategory.ATTITUDE),
    RESPONSIBILITY("책임 완수", TagCategory.ATTITUDE),
    EMPATHY("공감 능력", TagCategory.ATTITUDE),
    CHALLENGE("도전 의지", TagCategory.ATTITUDE),
    GOAL_ORIENTED("목표 지향", TagCategory.ATTITUDE);

    private final String labelKo;     // 화면에 보이는 한글
    private final TagCategory category;
}

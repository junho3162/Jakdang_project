package com.jakdang.config;

import com.jakdang.domain.Space;
import com.jakdang.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SpaceRepository spaceRepository;

    @Override
    public void run(String... args) throws Exception {
        // DB에 공간 정보가 없으면 자동으로 추가
        if (spaceRepository.count() == 0) {
            spaceRepository.save(Space.builder().spaceName("인문관 301호").location("인문관 3층").capacity(30).spaceType("강의실").build());
            spaceRepository.save(Space.builder().spaceName("공학관 102호").location("공학관 1층").capacity(50).spaceType("실습실").build());
            System.out.println("✅ 초기 강의실 데이터 등록 완료!");
        }
    }
}
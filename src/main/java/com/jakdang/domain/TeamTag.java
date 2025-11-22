// com.jakdang.domain.TeamTag
package com.jakdang.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamTag {

    POSTER_WEB_CONTENT("포스터/웹툰/콘텐츠"),
    PHOTO_VIDEO_UCC("사진/영상/UCC"),
    IDEA_PLANNING("아이디어/기획"),
    IT_RESEARCH("IT/학술/논문"),
    NAMING_SLOGAN("네이밍/슬로건"),
    ESSAY_LIT("에세이/수필/문학"),
    SPORTS_MUSIC("스포츠/음악"),
    ART_DESIGN_ARCH("미술/디자인/건축");

    private final String labelKo;
}

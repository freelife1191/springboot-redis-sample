package com.example.domain.recent;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;

@Schema(title = "검색 타입", description = """
        - AIR: 항공
        - CITY: 도시
        - HOTEL: 숙소
        - KEYWORD: 키워드
        - CATEGORY: 카테고리
        - PRODUCT: 상품
        - PLACE: 플레이스
        - AREA: 지역
        """)
public enum SearchType {
    AIR, CITY, HOTEL, KEYWORD, CATEGORY, PRODUCT, PLACE, AREA;

    public static List<SearchType> toList() {
        return Arrays.stream(SearchType.values()).toList();
    }
}
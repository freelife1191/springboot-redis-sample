package com.example.domain.recent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Schema(description = """
        카테고리(검색 위치)
           - COMM: 공통
           - HOTEL: 호텔 카테고리
           - TOACT: 투티 카테고리
           - PLACE: 여행지 찾기(플레이스) 카테고리
           - AIR: 항공 카테고리
           - PACKAGE: 패키지 카테고리
        """)
public enum Location {
    COMM,
    HOTEL,
    TOACT,
    PLACE,
    AIR,
    PACKAGE
}

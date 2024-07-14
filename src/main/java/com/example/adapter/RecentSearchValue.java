package com.example.adapter;

import com.example.domain.recent.RecentSearch;
import com.example.dto.recent.RecentSearchDto;
import com.example.exception.ParameterException;
import com.example.util.CacheUtil;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Created by mskwon on 2023/12/15.
 */
@Slf4j
@Getter
@ToString
@EqualsAndHashCode
public class RecentSearchValue {

    @Hidden
    private final String version; // 버전
    private final String nationCode; // 국가
    private final String searchType; // 검색 타입(AIR:항공, CITY:도시, HOTEL:숙소, KEYWORD:키워드, CATEGORY:카테고리, PRODUCT:상품, PLACE:플레이스, AREA:지역)
    private final String division; // 검색 구분 ex) parent_city
    private final String keyword; // 키워드 명 ex) 제주도 전체, 한국
    private final String id; // 검색 ID
    private final String options; // 옵션: 숙소(인원 수 성인2, 소인1)

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private final LocalDate from; // 시작일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private final LocalDate to; // 종료일

    private final String code; // 코드
    private final String data; // 데이터
    private final String name; // 이름
    private final String parent; // 부모 ID 또는 데이터
    private final String type; // 키워드 타입
    private final String etc; // 기타

    @Hidden
    private String idType; // 중복 값 제거시 idvisitor 값을 먼저제거하기 위해 정렬값으로 사용

    @Hidden
    private String ttl;

    @Hidden
    private LocalDateTime expiredAt;

    @Hidden
    private LocalDateTime updatedAt;

    public RecentSearchValue setIdType(String idType) {
        this.idType = idType;
        return this;
    }
    /**
     * 최근 검색 응답데이터 시간 데이터 설정
     * @param score 등록일시
     * @param ttl 만료까지 남은 시간
     * @return RecentSearchValue
     */
    public RecentSearchValue setTime(Long score, Long ttl, LocalDateTime now) {
        if (score != null && score > 0) {
            if (now == null) now = LocalDateTime.now();
            this.updatedAt = new Timestamp(score).toLocalDateTime();
            this.expiredAt = new Timestamp(Timestamp.valueOf(now).getTime() + (ttl * 1000)).toLocalDateTime();
        }
        this.ttl = CacheUtil.ttlToString(ttl);
        return this;
    }

    public static RecentSearchValue from(String version, String nationCode, String searchType, String division, String keyword, String id, String options, LocalDate from, LocalDate to, String code, String data, String name, String parent, String type, String etc) {
        return new RecentSearchValue(version, nationCode, searchType, division, keyword, id, options, from, to, code, data, name, parent, type, etc);
    }
    private RecentSearchValue(String version, String nationCode, String searchType, String division, String keyword, String id, String options, LocalDate from, LocalDate to, String code, String data, String name, String parent, String type, String etc) {
        if (StringUtils.isEmpty(version)) throw new ParameterException("version can't be null");
        if (StringUtils.isEmpty(searchType)) throw new ParameterException("searchType can't be null");
        if (StringUtils.isEmpty(keyword)) throw new ParameterException("keyword can't be null");
        if (StringUtils.isEmpty(id)) throw new ParameterException("id can't be null");
        this.version = version;
        this.nationCode = nationCode;
        this.searchType = searchType;
        this.division = division;
        this.keyword = keyword;
        this.id = id;
        this.options = options;
        this.from = from;
        this.to = to;
        this.code = code;
        this.data = data;
        this.name = name;
        this.parent = parent;
        this.type = type;
        this.etc = etc;
    }

    public static RecentSearchValue to(RecentSearchDto dto) throws DateTimeParseException{
        RecentSearch recentSearch = dto.getRecentSearch();
        return new RecentSearchValue(dto.getVersion(), recentSearch.getNationCode(),
                recentSearch.getSearchType().toString(),
                recentSearch.getDivision(),
                recentSearch.getKeyword(),
                recentSearch.getId(),
                recentSearch.getOptions(),
                parseLocalDate(recentSearch.getFrom()),
                parseLocalDate(recentSearch.getTo()),
                recentSearch.getCode(),
                recentSearch.getData(),
                recentSearch.getName(),
                recentSearch.getParent(),
                recentSearch.getType(),
                recentSearch.getEtc()
        );
    }

    private static LocalDate parseLocalDate(String recentSearch) throws DateTimeParseException {
        return StringUtils.isBlank(recentSearch) ? null :
                LocalDate.parse(recentSearch, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}

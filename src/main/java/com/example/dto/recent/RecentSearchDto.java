package com.example.dto.recent;

import com.example.domain.recent.Location;
import com.example.domain.recent.RecentSearch;
import com.example.domain.recent.SearchType;
import com.example.domain.recent.Site;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Objects;

import static com.example.domain.recent.SearchType.*;


/**
 * Created by mskwon on 2023/12/13.
 */
@Slf4j
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RecentSearchDto {
    @Hidden
    @JsonIgnore
    private String version;

    @Hidden
    @JsonIgnore
    @Setter
    private Site site;

    @Hidden
    @Schema(description = "검색 위치(AIR: 항공, HOTEL: 숙소 , TOACT: 투어&티켓, PLACE: 플레이스, PACKAGE: 패키지)")
    private Location location;

    @Hidden
    @JsonIgnore
    @Schema(description = "검색 타입(AIR[AIR: 항공], HOTEL[CITY: 도시, HOTEL: 호텔], TOACT[KEYWORD: 키워드, CITY: 도시, CATEGORY: 카테고리, PRODUCT: 상품], PLACE[PLACE: 플레이스], PACKAGE[KEYWORD: 키워드, CITY: 도시, CATEGORY: 카테고리(모상품), PRODUCT: 상품(자상품), AREA: 지역별])")
    private SearchType searchType;

    @Schema(description = "회원번호")
    private String memberNo;
    @Schema(description = "브라우저 id")
    private String idvisitor;

    @Setter
    // 검색을 누르면 _update 호출
    private RecentSearch recentSearch;

    @Hidden
    @JsonIgnore
    private HttpMethod httpMethod;

    @Builder
    public RecentSearchDto(String version, Site site, Location location, SearchType searchType, String memberNo, String idvisitor, RecentSearch recentSearch, HttpMethod httpMethod) {
        this.version = version;
        if (site != null) this.site = site;
        this.location = location;
        this.searchType = searchType;
        this.memberNo = memberNo;
        this.idvisitor = idvisitor;
        this.recentSearch = recentSearch;
        this.httpMethod = httpMethod;
        init();
    }

    public void init() {
        if (this.recentSearch == null) this.recentSearch = new RecentSearch();
        trim();
        initVersion();
        initSite();
        initLocation();
        initSearchType();
        initId();
    }

    public void initVersion() {
        if (StringUtils.isEmpty(this.version)) this.version = "1";
        if (StringUtils.isEmpty(this.recentSearch.getVersion())) this.recentSearch.setVersion("1");
    }

    public void initSite() {
        if (this.site != null) {
            return;
        }
        this.site = Site.TOUR;
    }

    public void initLocation() {
        if (this.location == null) this.location = this.recentSearch.getLocation();
    }

    public void initSearchType() {
        if (this.searchType == null) this.searchType = this.recentSearch.getSearchType();
    }

    /**
     * Filter 조건에 해당하는 Type의 경우 KeywordId를 생성
     */
    private void initId() {
        if (this.httpMethod == HttpMethod.GET || this.searchType == null || this.recentSearch.getKeyword() == null) return;
        if (verifyFilter()) return;
        // Filter 조건에 해당하는 Type의 경우 KeywordId를 생성
        this.recentSearch.generateId();
    }

    private boolean verifyFilter() {
        List<Boolean> filters = List.of(
                Objects.requireNonNull(this.searchType) == AIR,
                Objects.requireNonNull(this.searchType) == KEYWORD,
                Objects.requireNonNull(this.searchType) == AREA
        );
        return filters.stream().noneMatch(fileter -> fileter);
    }

    public void setLocationAndSearchType(String location, String searchType) {
        this.location = Location.valueOf(location);
        this.searchType = SearchType.valueOf(searchType);
        if (this.recentSearch == null)
            this.recentSearch = RecentSearch.builder()
                    .searchType(this.searchType)
                    .build();
        else this.recentSearch.setSearchType(this.searchType);
    }

    /**
     * 최근 검색 캐시 조회시 사용
     */
    public static RecentSearchDto from(HttpMethod httpMethod, String location, String memberNo, String idvisitor) {
        return from(httpMethod, location, memberNo, idvisitor, null);
    }
    public static RecentSearchDto from(HttpMethod httpMethod, String location, String memberNo, String idvisitor, Site site) {
        return RecentSearchDto.builder()
                .site(site)
                .location(location != null ? Location.valueOf(location) : null)
                .idvisitor(idvisitor)
                .memberNo(memberNo)
                .httpMethod(httpMethod)
                .build();
    }

    /**
     * 최근 검색 캐시 셋팅시 사용
     */
    public static RecentSearchDto from(HttpMethod httpMethod, String location, String memberNo, String idvisitor, Site site, SearchType searchType, String id) {
        return RecentSearchDto.builder()
                .site(site)
                .location(location != null ? Location.valueOf(location) : null)
                .idvisitor(idvisitor)
                .memberNo(memberNo)
                .searchType(searchType)
                .recentSearch(RecentSearch.builder().id(id).build())
                .httpMethod(httpMethod)
                .build();
    }

    private void trim() {
        this.memberNo = StringUtils.trim(this.memberNo);
        this.idvisitor = StringUtils.trim(this.idvisitor);
        if (this.recentSearch == null) return;
        this.recentSearch.setDivision(StringUtils.trim(this.recentSearch.getDivision()));
        this.recentSearch.setKeyword(StringUtils.trim(this.recentSearch.getKeyword()));
        this.recentSearch.setId(StringUtils.trim(this.recentSearch.getId()));
        this.recentSearch.setOptions(StringUtils.trim(this.recentSearch.getOptions()));
        this.recentSearch.setCode(StringUtils.trim(this.recentSearch.getCode()));
        this.recentSearch.setData(StringUtils.trim(this.recentSearch.getData()));
        this.recentSearch.setName(StringUtils.trim(this.recentSearch.getName()));
        this.recentSearch.setParent(StringUtils.trim(this.recentSearch.getParent()));
        this.recentSearch.setType(StringUtils.trim(this.recentSearch.getType()));
        this.recentSearch.setEtc(StringUtils.trim(this.recentSearch.getEtc()));
    }
}

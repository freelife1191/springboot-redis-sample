package com.example.domain.recent;

import com.example.adapter.RecentSearchValue;
import com.example.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Created by mskwon on 2023/12/09.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(title = "최근 검색")
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentSearch {

    @Hidden
    @JsonIgnore
    private Site site;

    @Hidden
    @JsonIgnore
    @Schema(title = "버전", hidden = true)
    private String version;

    @Schema(title = "검색 위치", description = "AIR:항공, HOTEL:숙소, TOACT:투어&티켓, PLACE:플레이스, PACKAGE:패키지")
    private Location location;

    @Schema(title = "검색 타입", description = "AIR:항공, CITY:도시, HOTEL:숙소, KEYWORD:키워드, CATEGORY:카테고리, PRODUCT:상품, PLACE:플레이스, AREA:지역")
    private SearchType searchType;

    @Schema(title = "국가", example = "KR")
    private String nationCode;

    @Schema(title = "검색 구분", description = "ex) hotel")
    private String division;

    @Schema(title = "키워드 명", description = "ex) 제주도 전체, 한국")
    private String keyword;

    @Schema(title = "검색 ID")
    private String id;

    @Schema(title = "옵션", description = "숙소(객실 인원 ex) 2~2,3~10,4~11)")
    private String options; // 옵션: 숙소(인원 수 성인2, 소인1)

    @Schema(title = "시작일", description = "ex) 2024-12-28")
    @Pattern(regexp="^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$", message="날짜 형식이 올바르지 않습니다. 날짜형식: yyyy-MM-dd 예) 2024-01-01")
    private String from;

    @Schema(title = "종료일", description = "ex) 2024-12-31")
    @Pattern(regexp="^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$", message="날짜 형식이 올바르지 않습니다. 날짜형식: yyyy-MM-dd 예) 2024-01-01")
    private String to;

    @Schema(title = "코드", description = "ex) jejudo")
    private String code; // 코드 옵션

    @Schema(title = "데이터", description = "데이터 ex) {\"ICN\":\"서울(인천)\",\"NRT\":\"도쿄(나리타)\",\"LAX\":\"로스앤젤레스\"}")
    private String data; // 데이터 옵션

    @Schema(title = "이름 또는 표시 이름")
    private String name;

    @Schema(title = "부모 ID 또는 데이터")
    private String parent;

    @Schema(title = "키워드 타입")
    private String type;

    @Schema(title = "기타")
    private String etc;

    @Schema(title = "최근 검색 TTL", description = "만료까지 남은시간")
    private String ttl;

    @Schema(title = "최근 검색 만료일시", description = "만료될 일시")
    private LocalDateTime expiredAt;

    @Schema(title = "최근 검색 수정일시")
    private LocalDateTime updatedAt;

    public RecentSearch setSite(Site site) {
        this.site = site;
        return this;
    }

    public static RecentSearch to(RecentSearchValue value, String location) {
        return RecentSearch.builder()
                .version(value.getVersion())
                .location(Location.valueOf(location))
                .searchType(SearchType.valueOf(value.getSearchType()))
                .nationCode(value.getNationCode())
                .division(value.getDivision())
                .keyword(value.getKeyword())
                .id(value.getId())
                .options(value.getOptions())
                .from(value.getFrom() != null ? value.getFrom().toString() : null)
                .to(value.getTo() != null ? value.getTo().toString() : null)
                .code(value.getCode())
                .data(parseData(value.getData()))
                .name(value.getName())
                .parent(value.getParent())
                .type(value.getType())
                .etc(value.getEtc())
                .ttl(value.getTtl())
                .expiredAt(value.getExpiredAt())
                .updatedAt(value.getUpdatedAt())
                .build();
    }

    public void generateId() {
        String keyword = this.keyword;
        switch (this.searchType) {
            case AIR -> {
                keyword += this.options + convertData(this.data);
                this.id = String.valueOf(keyword.hashCode());
            }
            case KEYWORD, AREA -> this.id = String.valueOf(this.keyword.hashCode());
        }
        log.debug("generateId: {}, hashcode: {}, keyword_id: {}", keyword, keyword.hashCode(), this.id);
    }

    /**
     * JSON Cache 사이즈 축소를 위한 변환
     * @param data
     * @return
     */
    public static String convertData(String data) {
        if (StringUtils.isBlank(data)) return null;
        Map<String, String> dataMap = JsonUtils.toMapperObject(data, new TypeReference<LinkedHashMap<String, String>>() {});
        if (dataMap == null) return null;
        return dataMap.keySet().stream()
                .map(key -> key + "=" + dataMap.get(key))
                .collect(Collectors.joining("⎮"));
    }

    /**
     * JSON Cache 사이즈 축소를 위해 변환한 데이터 Parsing
     *
     * @param data 캐시에서 조회한 데이터
     * @return json 타입으로 변환한 데이터
     */
    public static String parseData(String data) {
        if (StringUtils.isBlank(data)) return null;

        // Parsing 할 데이터가 없다면 기존 데이터 그대로 사용
        String[] datas = data.split("⎮");
        if (datas.length < 1) return data;

        // Parsing 실패시 기존 데이터 그대로 사용
        try {
            return JsonUtils.toMapper(Arrays.stream(datas)
                    .map(v -> v.split("="))
                    .collect(toMap(s -> s[0], s -> s[1], (v1, v2) -> v1, LinkedHashMap::new)));
        } catch (Exception e) {
            return data;
        }
    }



    /**
     * 각 Inventory 별로 사용되는 Type 정의
     */
    @Schema(title = "최근 검색 카테고리별 인벤토리", description = """
        - AIR: 항공 인벤토리
            - 카테고리: AIR
            - 검색타입: AIR
        - HOTEL: 숙소 인벤토리
            - 카테고리: HOTEL
            - 검색타입: HOTEL, CITY
        - TOACT: 투어&액티비티 인벤토리
            - 카테고리: TOACT
            - 검색타입: KEYWORD, CITY, CATEGORY, PRODUCT
        - PLACE: 여행지 찾기(플레이스) 인벤토리
            - 카테고리: PLACE
            - 검색타입: PLACE
        - PACKAGE: 패키지 인벤토리
            - 카테고리: PACKAGE
            - 검색타입: KEYWORD, CITY, CATEGORY, PRODUCT, AREA
        검색 타입(AIR[AIR: 항공], HOTEL[CITY: 도시, HOTEL: 호텔], TOACT[KEYWORD: 키워드, CITY: 도시, CATEGORY: 카테고리, PRODUCT: 상품], PLACE[PLACE: 플레이스], PACKAGE[KEYWORD: 키워드, CITY: 도시, CATEGORY: 카테고리(모상품), PRODUCT: 상품(자상품), AREA: 지역별])
        """)
    @Getter
    @AllArgsConstructor
    public enum Inventory {
        AIR(
                location(Location.AIR),
                types(SearchType.AIR)),
        HOTEL(
                location(Location.HOTEL),
                types(SearchType.HOTEL, SearchType.CITY)),
        TOACT(
                location(Location.TOACT),
                types(SearchType.KEYWORD, SearchType.CITY, SearchType.CATEGORY, SearchType.PRODUCT)),
        PLACE(
                location(Location.PLACE),
                types(SearchType.PLACE)),
        PACKAGE(
                location(Location.PACKAGE),
                types(SearchType.KEYWORD, SearchType.CITY, SearchType.CATEGORY, SearchType.PRODUCT, SearchType.AREA)),
        ;

        private final Location location;
        private final SearchType[] searchTypes;

        public static Inventory getTypesByInventory(Inventory inventory) {
            return Arrays.stream(Inventory.values())
                    .filter(v -> v == inventory)
                    .findFirst()
                    .orElse(null);
        }

        public static List<Location> getLocationBySearchType(SearchType searchType) {
            return Arrays.stream(Inventory.values())
                    .filter(v -> Arrays.asList(v.getSearchTypes()).contains(searchType))
                    .map(Inventory::getLocation)
                    .toList();
        }

        public static boolean verifyLocationAndSearchType(Location location, SearchType searchType) {
            return getLocationBySearchType(searchType).contains(location);
        }

        private static <T> T location(T location) {
            return location;
        }

        private static <T> T[] types(T... types) {
            return types;
        }
    }
}
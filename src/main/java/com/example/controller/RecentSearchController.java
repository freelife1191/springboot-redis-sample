package com.example.controller;

import com.example.domain.recent.RecentSearch;
import com.example.domain.recent.SearchType;
import com.example.domain.recent.Site;
import com.example.dto.recent.RecentSearchDto;
import com.example.service.recent.RecentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by mskwon on 2023/12/15.
 */
@Slf4j
@RestController
@Tag(name = "최근 검색 API", description = "RecentSearchController")
@RequestMapping("/recent-search")
@RequiredArgsConstructor
public class RecentSearchController {

    private final CustomCollectionValidator customCollectionValidator;
    private final RecentSearchService recentSearchService;

    @Operation(summary = "01. 최근 검색 조회", description = """
            가장 최근에 검색한 10개 검색기록을 조회
            
            - **필수 입력**: `member_no` or `idvisitor`
            """)
    @GetMapping("/location/{location}")
    public ResponseEntity<List<RecentSearch>> getRecentSearch(
            @Parameter(description = "검색 위치(AIR: 항공, HOTEL: 숙소 , TOACT: 투어&티켓, PLACE: 플레이스, PACKAGE: 패키지)", example = "HOTEL",
                    schema = @Schema(description = "검색 위치",type = "string", allowableValues = {"AIR", "HOTEL", "TOACT", "PLACE", "PACKAGE"}))
            @PathVariable(value = "location", required = false) String location,
            @Parameter(description = "회원번호", example = "20230822101306776")
            @RequestParam(value = "member_no", required = false) String memberNo,
            @Parameter(description = "브라우저 id", example = "eb30dfe378b38c58")
            @RequestParam(value = "idvisitor", required = false) String idvisitor
    ) {
        return ResponseEntity.ok(recentSearchService.get(Site.TOUR, location, memberNo, idvisitor));
    }

    private static final String POST_EXAM = """
            {
              "member_no" : "20230822101306776",
              "idvisitor" : "eb30dfe378b38c58",
              "recent_search" : {
                "nation_code" : "KR",
                "division" : "hotel",
                "keyword" : "파르나스 호텔 제주, 서귀포시, 한국",
                "id" : "714643",
                "options" : "2~2,3~10,4~11",
                "from" : "2024-12-28",
                "to" : "2024-12-31"
              }
            }
            """;

    @Operation(summary = "02. 최근 검색 등록 및 업데이트", description = """
            # 공통 파라메터 설명
            - `member_no`: 회원 번호, 로그인 되어 있는 경우에 넣어줘야 함
            - `idvisitor`(**필수**) 브라우저 ID
            
            ### 최근검색 전체 파라메터
            
            필요시 필수 파라메터 외 다른 파라메터를 추가해서 사용
            
            - `recent_search`
                - `nation_code`: 국가코드
                - `division`: 검색 구분
                - `keyword`: 키워드명
                - `id`: 키워드 ID
                - `options`: 옵션
                - `from`: 숙박 시작일 (YYYY-MM-DD)
                - `to`: 숙박 종료일 (YYYY-MM-DD)
                - `code`: 코드명
                - `data`: 데이터 (JSON 및 Array 데이터)
                - `name`: 명칭
                - `parent`: 부모 ID 또는 데이터
                - `type`: 키워드 타입(키워드 구분을 위해 필요시 사용하는 기타 타입, 검색 타입과는 다름)
                - `etc`: 기타 데이터
    
            
            # 숙소 파라메터 설명
            
            - `recent_search`: 등록할 최근 검색 데이터 객체
                - `nation_code`(**필수**): 국가코드 ex) KR
                - `division`(**필수**): 검색 구분 ex) hotel
                - `keyword`(**필수**): 키워드명 ex) 제주도 전체, 한국
                - `id`(**필수**): 키워드 ID (city_master_id, htl_master_id)
                - `options`(**필수**): 옵션: 숙소(객실 인원 (객실당 성인~소인))
                - `from`(**필수**): 숙박 시작일 (YYYY-MM-DD)
                - `to`(**필수**): 숙박 종료일 (YYYY-MM-DD)
             
            ### ex) 호텔 최근 검색 등록
            `location`: **hotel**, `type`: **hotel**
            
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "nation_code": "KR",
                "division": "hotel",
                "keyword": "파르나스 호텔 제주, 서귀포시, 한국",
                "id": "714643",
                "options": "2~2,3~10,4~11",
                "from": "2024-12-28",
                "to": "2024-12-31"
              }
            }
            ```
             
            ### ex) 도시 최근 검색 등록
            `location`: **hotel**, `type`: **city**
            
            ```
            {
              "member_no" : "20230822101306776",
              "idvisitor" : "eb30dfe378b38c58",
              "recent_search" : {
                 "nation_code": "KR",
                 "division": "parent_city",
                 "keyword": "제주도 전체, 한국",
                 "id": "100249",
                 "options": "2,1~5,1",
                 "from": "2024-12-28",
                 "to": "2024-12-31"
               }
            }
            ```
             
            # 투어&티켓 파라메터 설명
            
            ### ex) 키워드 최근 검색 등록
            `location`: **toact**, `type`: **keyword**
            
            - `keyword`(**필수**): 키워드
               
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "keyword": "제주도"
              }
            }
            ```
            
            ### ex) 도시 최근 검색 등록
            `location`: **toact**, `type`: **city**
            
            - `keyword`(**필수**): 도시명
            - `id`(**필수**): 도시 코드 (city_master_id)
            
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "keyword": "제주도",
                "id": "100249"
              }
            }
            ```
            
            ### ex) 카테고리 최근 검색 등록
            `location`: **toact**, `type`: **category**
            
            - `keyword`(**필수**): 카테고리명
            - `id`(**필수**): 카테고리 ID (category_id)
            - `parent`: 부모 ID
            
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "keyword": "셀럽테마투어",
                "id": "CG61",
                "parent": "CAT2000000140"
              }
            }
            ```
            
            ### ex) 상품 최근 검색 등록
            `location`: **toact**, `type`: **product**
            
            - `keyword`(**필수**): 상품명
            - `id`(**필수**): 상품 ID (product_id)
            
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "keyword": "제주도 서핑 998서프 삼양 이용권",
                "id": "PRD3000610651"
              }
            }
            ```
            
            # 플레이스 파라메터 설명
            
            ### ex) 플레이스 최근 검색 등록
            `location`: **place**, `type`: **place**
            
            - `keyword`(**필수**): 키워드
            - `id`(**필수**): 키워드 ID (city_master_id)
            - `code`: 유니코드명 ex) jejudo
               
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "keyword": "제주도",
                "id": "100249",
                "code:": "jejudo"
              }
            }
            ```
            
            # 항공 파라메터 설명
            
            ### ex) 항공 최근 검색 등록
            `location`: **air**, `type`: **air**
            
            - `keyword`(**필수**): 출발 도착 데이터
            - `options`(**필수**): 옵션(성인,아동,유아|클래스(일반석:Y,프리미엄일반석:P,비즈니스석:C,일등석:F)|타입(왕복:RT,편도:OW,다구간:MD)
            - `data`(**필수**): JSON 데이터"
               
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "keyword": "ICN-20240213-NRT,NRT-20240214-ROM,ROM-20240215-PAR,PAR-20240216-BCN",
                "options": "2,1,1|F|MD",
                "data": "{\\"ICN\\":\\"서울(인천)\\",\\"NRT\\":\\"도쿄(나리타)\\",\\"ROM\\":\\"로마\\",\\"PAR\\":\\"파리\\",\\"BCN\\":\\"바르셀로나\\"}"
              }
            }
            ```
            
            # 패키지 파라메터 설명
            
            ### ex) 키워드 최근 검색 등록
            `location`: **package**, `type`: **keyword**
            
            - `keyword`(**필수**): 키워드명
            - `name`: 표시 이름
               
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "keyword": "캄보디아",
                "name": "4911,앙코르와트"
              }
            }
            ```
            
            ### ex) 도시 최근 검색 등록
            `location`: **package**, `type`: **city**
            
            - `nation_code`: 국가 코드
            - `keyword`(**필수**): 도시명
            - `id`(**필수**): 도시 코드
            
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "nation_code": "JP",
                "keyword": "오사카, 일본",
                "id": "12495",
              }
            }
            ```
            
            ### ex) 카테고리(모상품) 최근 검색 등록
            `location`: **package**, `type`: **category**
            
            - `keyword`(**필수**): 상품명
            - `id`(**필수**): 모상품 코드(represent_product_code)
            - `from`: 출발일 (YYYY-MM-DD)
            - `to`: 도착일 (YYYY-MM-DD)
            
            ```
            {
              "member_no": "20230822101306776",
              "idvisitor": "eb30dfe378b38c58",
              "recent_search": {
                "keyword": "[기타큐슈 3일] 기분전환 그리고 휴식, 벳부+유후인+기타큐슈 (기본요금제)",
                "id": "21560-106783",
                "from": "2024-04-06",
                "to": "2024-10-25"
              }
            }
            ```
            
            ### ex) 상품(자상품) 최근 검색 등록
            `location`: **package**, `type`: **product**
            
            - `keyword`(**필수**): 상품명
            - `id`(**필수**): 자상품 코드 (sale_product_code)
            - `parent`(**필수**): 모상품 코드 (represent_product_code)
            - `from`: 출발일 (YYYY-MM-DD)
            - `to`: 도착일 (YYYY-MM-DD)
            
            ```
            {
              "member_no" : "20230822101306776",
              "idvisitor" : "eb30dfe378b38c58",
              "recent_search" : {
                "keyword" : "[기타큐슈 3일] 기분전환 그리고 휴식, 벳부+유후인+기타큐슈 (기본요금제)",
                "id" : "4796701",
                "parent" : "21560-106783",
                "from": "2024-04-06",
                "to": "2024-10-25"
              }
            }
            ```
            
            ### ex) 지역별 최근 검색 등록
            `location`: **package**, `type`: **area**
            
            - `keyword`(**필수**): 지역명
            - `data`(**필수**): 도시 배열 데이터
            
            ```
            {
              "member_no" : "20230822101306776",
              "idvisitor" : "eb30dfe378b38c58",
              "recent_search" : {
                "keyword" : "도쿄",
                "data" : "100276,12287,100285"
              }
            }
            ```
            """)
    @PostMapping("/location/{location}/search_type/{search_type}")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = POST_EXAM)))
    public ResponseEntity<HttpStatus> setRecentSearch(
            @Parameter(description = "검색 위치(AIR: 항공, HOTEL: 숙소 , TOACT: 투어&티켓, PLACE: 플레이스, PACKAGE: 패키지)", example = "HOTEL",
                    schema = @Schema(description = "검색 위치",type = "string", allowableValues = {"AIR", "HOTEL", "TOACT", "PLACE", "PACKAGE"}))
            @PathVariable(value = "location") String location,
            @Parameter(description = "검색 타입(AIR[AIR: 항공], HOTEL[CITY: 도시, HOTEL: 호텔], TOACT[KEYWORD: 키워드, CITY: 도시, CATEGORY: 카테고리, PRODUCT: 상품], PLACE[PLACE: 플레이스], PACKAGE[KEYWORD: 키워드, CITY: 도시, CATEGORY: 카테고리(모상품), PRODUCT: 상품(자상품), AREA: 지역별])", example = "HOTEL",
                    schema = @Schema(description = "검색 타입",type = "string", allowableValues = {"AIR", "CITY", "HOTEL", "KEYWORD", "CATEGORY", "PRODUCT", "PLACE", "AREA"}))
            @PathVariable(value = "search_type") String searchType,
            @RequestBody @Valid RecentSearchDto dto,
            BindingResult bindingResult
    ) throws BindException {
        dto.setLocationAndSearchType(location, searchType);

        // Collection 의 경우 @Valid 로 유효성 검증이 되지 않아 직접 validate
        customCollectionValidator.validate(dto.getRecentSearch(), bindingResult);

        // Collection 유효성 검증 실패에 대한 예외처리
        if (bindingResult.hasErrors())
            throw new BindException(bindingResult);

        recentSearchService.set(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "03. 최근 검색 단건삭제", description = "- **필수 입력**: `member_no` or `idvisitor`, `search_type`, `id`")
    @DeleteMapping("/location/{location}/search_type/{search_type}/id/{id}")
    public ResponseEntity<HttpStatus> deleteRecentSearch(
            @Parameter(description = "검색 위치(AIR: 항공, HOTEL: 숙소 , TOACT: 투어&티켓, PLACE: 플레이스, PACKAGE: 패키지)", example = "HOTEL",
                    schema = @Schema(description = "검색 위치",type = "string", allowableValues = {"AIR", "HOTEL", "TOACT", "PLACE", "PACKAGE"}))
            @PathVariable(value = "location") String location,
            @Parameter(description = "검색 타입(AIR[AIR: 항공], HOTEL[CITY: 도시, HOTEL: 호텔], TOACT[KEYWORD: 키워드, CITY: 도시, CATEGORY: 카테고리, PRODUCT: 상품], PLACE[PLACE: 플레이스], PACKAGE[KEYWORD: 키워드, CITY: 도시, CATEGORY: 카테고리(모상품), PRODUCT: 상품(자상품), AREA: 지역별])", example = "HOTEL",
                    schema = @Schema(description = "검색 타입",type = "string", allowableValues = {"AIR", "CITY", "HOTEL", "KEYWORD", "CATEGORY", "PRODUCT", "PLACE", "AREA"}))
            @PathVariable(value = "search_type") String searchType,
            @Parameter(description = "삭제할 키워드 ID(city_master_id, htl_master_id)", example = "714643")
            @PathVariable(value = "id") String id,
            @Parameter(description = "회원번호", example = "20230822101306776")
            @RequestParam(value = "member_no", required = false) String memberNo,
            @Parameter(description = "브라우저 id", example = "eb30dfe378b38c58")
            @RequestParam(value = "idvisitor", required = false) String idvisitor
    ) {
        recentSearchService.delete(Site.TOUR, location, memberNo, idvisitor, SearchType.valueOf(searchType), id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "04. 최근 검색 전체삭제", description = "- **필수 입력**: `member_no` or `idvisitor`")
    @DeleteMapping("/location/{location}")
    public ResponseEntity<HttpStatus> deleteAllRecentSearch(
            @Parameter(description = "검색 위치(AIR: 항공, HOTEL: 숙소 , TOACT: 투어&티켓, PLACE: 플레이스, PACKAGE: 패키지)", example = "HOTEL",
                    schema = @Schema(description = "검색 위치",type = "string", allowableValues = {"AIR", "HOTEL", "TOACT", "PLACE", "PACKAGE"}))
            @PathVariable(value = "location") String location,
            @Parameter(description = "회원번호", example = "20230822101306776")
            @RequestParam(value = "member_no", required = false) String memberNo,
            @Parameter(description = "브라우저 id", example = "eb30dfe378b38c58")
            @RequestParam(value = "idvisitor", required = false) String idvisitor
    ) {
        recentSearchService.delete(Site.TOUR, location, memberNo, idvisitor, null, null);
        return ResponseEntity.ok().build();
    }
}

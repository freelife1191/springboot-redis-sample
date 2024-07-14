package com.example.service;

import com.example.core.BaseTest;
import com.example.domain.recent.Location;
import com.example.domain.recent.RecentSearch;
import com.example.domain.recent.SearchType;
import com.example.domain.recent.Site;
import com.example.dto.recent.RecentSearchDto;
import com.example.service.recent.RecentSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

/**
 * Created by mskwon on 2023/12/24.
 */
class RecentSearchServiceTest extends BaseTest {

    @Autowired
    private RecentSearchService recentSearchService;

    public RecentSearchServiceTest(MockMvc mockMvc) {
        super(mockMvc);
    }

    private final String idvisitor = "eb30dfe378b38c58";
    // private final String memberNo = "20230822101306775";
    private final String memberNo = "20230822101306776";

    @BeforeEach
    void setUp() {
    }

    // todo: 투티는 키워드, 도시, 카테고리, 상품 네가지 형식으로 캐시를 최대 10개까지 보관하도록 기능 개선
    @Test
    @Description("투티 키워드 캐시 저장")
    void setKeyword() {
        RecentSearch recentSearch = RecentSearch.builder()
                .location(Location.HOTEL)
                .searchType(SearchType.KEYWORD)
                .keyword("제주도")
                .build();
        RecentSearchDto dto = RecentSearchDto.builder()
                .idvisitor(idvisitor)
                .memberNo(memberNo)
                .recentSearch(recentSearch)
                .build();
        System.out.println(dto);

        recentSearchService.set(dto);
    }

    @Test
    void setCity() {
        RecentSearch recentSearch = RecentSearch.builder()
                .location(Location.HOTEL)
                .searchType(SearchType.CITY)
                .keyword("제주도")
                .id("100249")
                .build();
        RecentSearchDto dto = RecentSearchDto.builder()
                .idvisitor(idvisitor)
                .memberNo(memberNo)
                .recentSearch(recentSearch)
                .build();
        System.out.println(dto);

        recentSearchService.set(dto);
    }

    @Test
    void setCategory() {
        RecentSearch recentSearch = RecentSearch.builder()
                .location(Location.TOACT)
                .searchType(SearchType.CATEGORY)
                .id("CG61")
                .keyword("셀럽테마투어")
                .build();
        RecentSearchDto dto = RecentSearchDto.builder()
                .idvisitor(idvisitor)
                .memberNo(memberNo)
                .recentSearch(recentSearch)
                .build();
        System.out.println(dto);

        recentSearchService.set(dto);
    }

    @Test
    void setProduct() {
        RecentSearch recentSearch = RecentSearch.builder()
                .location(Location.TOACT)
                .searchType(SearchType.PRODUCT)
                .id("PRD3000610651")
                .keyword("제주도 서핑 998서프 삼양 이용권")
                .build();
        RecentSearchDto dto = RecentSearchDto.builder()
                .idvisitor(idvisitor)
                .memberNo(memberNo)
                .recentSearch(recentSearch)
                .build();
        System.out.println(dto);

        recentSearchService.set(dto);
    }

    @Test
    void get() {
        List<RecentSearch> recentSearchList = recentSearchService.get(Site.TOUR, Location.TOACT.name().toLowerCase(), memberNo, idvisitor);
        System.out.println(recentSearchList);
    }

    @Test
    void delete() {
    }

    @Test
    void inventoryTypeCheck() {
        List<Location> locationByType = RecentSearch.Inventory.getLocationBySearchType(SearchType.KEYWORD);
        System.out.println(locationByType);
        boolean b = RecentSearch.Inventory.verifyLocationAndSearchType(Location.PACKAGE, SearchType.KEYWORD);
        System.out.println(b);
    }

    @Test
    void getRecentSearchListTest() {
        // recentSearchService.getRecentSearchList(Site.tourvis, "", "d24897c064c7f356").forEach(System.out::println);
        recentSearchService.getRecentSearches(Site.TOUR, memberNo, idvisitor);
    }

}
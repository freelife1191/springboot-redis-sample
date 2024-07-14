package com.example.controller;

import com.example.core.BaseTest;
import com.example.domain.recent.Location;
import com.example.domain.recent.RecentSearch;
import com.example.domain.recent.SearchType;
import com.example.dto.recent.RecentSearchDto;
import com.example.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by mskwon on 2023/12/15.
 */
class RecentSearchControllerTest extends BaseTest {

    private String PrefixUrl = "/recent-search";

    private RecentSearchDto recentSearchDto;

    public RecentSearchControllerTest(MockMvc mockMvc) {
        super(mockMvc);
    }

    @BeforeEach
    void setUp() {
        recentSearchDto = RecentSearchDto.builder()
                .idvisitor("eb30dfe378b38c58")
                .memberNo("20230822101306776")
                .build();
    }

    @Test
    void setDataTest() {
        recentSearchDto.setRecentSearch(
                JsonUtils.toMapperObject(getJson(SearchFile.HOTEL.getName()), RecentSearch.class));
        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));
    }

    @Test
    void setRecentSearchHotelMulti() {
        IntStream.rangeClosed(1, 10).forEach(i -> {
            Integer htlMasterId = 1_000_000+i;
            recentSearchDto.setRecentSearch(
                    JsonUtils.toMapperObject(getJson(SearchFile.HOTEL.getName()), RecentSearch.class));
            recentSearchDto.getRecentSearch().setNationCode("KR");
            recentSearchDto.getRecentSearch().setKeyword("서귀포 블라섬펜션_"+i);
            recentSearchDto.getRecentSearch().setId(htlMasterId.toString());

            System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

            try {
                mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.HOTEL, SearchType.HOTEL.name())
                                .content(JsonUtils.toMapper(recentSearchDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Test
    void setRecentSearchCityMulti() {
        IntStream.rangeClosed(1, 10).forEach(i -> {
            Integer cityMasterId = 500_000+i;
            recentSearchDto.setRecentSearch(
                    JsonUtils.toMapperObject(getJson(SearchFile.CITY.getName()), RecentSearch.class));
            recentSearchDto.getRecentSearch().setNationCode("KR");
            recentSearchDto.getRecentSearch().setKeyword("제주도 전체_"+i);
            recentSearchDto.getRecentSearch().setId(cityMasterId.toString());

            System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

            try {
                mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.HOTEL, SearchType.CITY.name())
                                .content(JsonUtils.toMapper(recentSearchDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void setRecentSearchHotel() throws Exception {
            recentSearchDto.setRecentSearch(
                    JsonUtils.toMapperObject(getJson(SearchFile.HOTEL.getName()), RecentSearch.class));

            System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

            mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.HOTEL, SearchType.HOTEL.name())
                            .content(JsonUtils.toMapper(recentSearchDto))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();
    }

    @Test
    void setRecentSearchCity() throws Exception {
            recentSearchDto.setRecentSearch(
                    JsonUtils.toMapperObject(getJson(SearchFile.CITY.getName()), RecentSearch.class));

            System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

            mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.HOTEL, SearchType.CITY.name())
                            .content(JsonUtils.toMapper(recentSearchDto))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();
    }

    @Test
    void getRecentSearch() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        ResultActions result = mockMvc.perform(get(PrefixUrl+"/location/{location}", Location.HOTEL)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print());
        String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<RecentSearch> recentSearchList = JsonUtils.toMapperObject(responseBody, new TypeReference<>() {});
        recentSearchList.forEach(it -> System.out.println(JsonUtils.toMapperPretty(it)));
    }

    @Test
    @Description("단건 삭제")
    void deleteRecentSearch() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        String type = SearchType.HOTEL.name();
        String keywordId = "1552576";
        mockMvc.perform(delete(PrefixUrl+"/location/{location}/search_type/{search_type}/id/{id}", Location.HOTEL, type, keywordId)
                        .params(paramMap))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();
    }

    @Test
    @Description("전체 삭제")
    void deleteAllRecentSearch() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        mockMvc.perform(delete(PrefixUrl+"/location/{location}", Location.HOTEL)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchKeyword() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("코엑스 아쿠아리움RECOM")
                        .build());

        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.TOACT, SearchType.KEYWORD.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchToactCity() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("제주도")
                        .id("100249")
                        .build());
        // recentSearch.getRecentSearch().setKeyword("서귀포시");
        // recentSearch.getRecentSearch().setId("15754");
        // recentSearch.getRecentSearch().setId("8176");

        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.TOACT, SearchType.CITY.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchCategory() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("셀럽테마투어")
                        .id("CG61")
                        .build());
        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.TOACT, SearchType.CATEGORY.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchProduct() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("제주도 서핑 998서프 삼양 이용권")
                        .id("PRD3000610651")
                        .build());
        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.TOACT, SearchType.PRODUCT.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRecentSearchToact() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        ResultActions result = mockMvc.perform(get(PrefixUrl+"/location/{location}", Location.TOACT)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print());
        String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<RecentSearch> recentSearchList = JsonUtils.toMapperObject(responseBody, new TypeReference<>() {});
        recentSearchList.forEach(it -> System.out.println(JsonUtils.toMapperPretty(it)));
    }

    @Test
    @Description("TOACT 단건 삭제")
    void deleteRecentSearchToact() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        String type = SearchType.CATEGORY.name();
        String keywordId = "CG61";
        mockMvc.perform(delete(PrefixUrl+"/location/{location}/search_type/{search_type}/id/{id}", Location.TOACT, type, keywordId)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    @Description("전체 삭제")
    void deleteAllRecentSearchToact() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        mockMvc.perform(delete(PrefixUrl+"/location/{location}", Location.TOACT)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchPlace() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("제주도")
                        .id("100249")
                        .code("jejudo")
                        .build());
        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.PLACE, SearchType.PLACE.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchPlaceMulti() throws Exception {
        IntStream.rangeClosed(0,12).forEach(i -> {
            recentSearchDto.setRecentSearch(
                    RecentSearch.builder()
                            .keyword("제주도"+i)
                            .id("100249")
                            .code("jejudo"+i)
                            .build());
            System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

            try {
                mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.PLACE, SearchType.PLACE.name())
                                .content(JsonUtils.toMapper(recentSearchDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void getRecentSearchPlace() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        ResultActions result = mockMvc.perform(get(PrefixUrl+"/location/{location}", Location.PLACE)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print());
        String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<RecentSearch> recentSearchList = JsonUtils.toMapperObject(responseBody, new TypeReference<>() {});
        recentSearchList.forEach(it -> System.out.println(JsonUtils.toMapperPretty(it)));
    }

    @Test
    void deleteRecentSearchPlace() throws Exception {
        String type = SearchType.PLACE.name();
        String id = "10211";
        // String keywordId = "1577005394";
        // String keywordId = "1577005391";
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        mockMvc.perform(delete(PrefixUrl+"/location/{location}/search_type/{search_type}/id/{id}", Location.PLACE, type, id)
                        .params(paramMap))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();
    }

    @Test
    @Description("전체 삭제")
    void deleteAllRecentSearchPlace() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        mockMvc.perform(delete(PrefixUrl+"/location/{location}", Location.PLACE)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchAir() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("ICN-20240213-NRT,NRT-20240214-ROM,ROM-20240215-PAR,PAR-20240216-BCN")
                        .options("2,1,1|F|MD")
                        .data("{\"ICN\":\"서울(인천)\",\"NRT\":\"도쿄(나리타)\",\"ROM\":\"로마\",\"PAR\":\"파리\",\"BCN\":\"바르셀로나\"}")
                        .build());
        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.AIR, SearchType.AIR.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchAirMulti() throws Exception {
        IntStream.rangeClosed(1,10).forEach(i -> {
            recentSearchDto.setRecentSearch(
                    RecentSearch.builder()
                            .keyword("ICN-20240213-NRT,NRT-20240214-ROM,ROM-20240215-PAR,PAR-20240216-BCN")
                            .options("2,1,"+i+"|F|MD")
                            .data("{\"ICN\":\"서울(인천)\",\"NRT\":\"도쿄(나리타)\",\"ROM\":\"로마\",\"PAR\":\"파리\",\"BCN\":\"바르셀로나\"}")
                            .build());
            System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

            try {
                mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.AIR, SearchType.AIR.name())
                                .content(JsonUtils.toMapper(recentSearchDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void getRecentSearchAir() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        ResultActions result = mockMvc.perform(get(PrefixUrl+"/location/{location}", Location.AIR)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print());
        String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<RecentSearch> recentSearchList = JsonUtils.toMapperObject(responseBody, new TypeReference<>() {});
        recentSearchList.forEach(it -> System.out.println(JsonUtils.toMapperPretty(it)));
    }

    @Test
    @Description("전체 삭제")
    void deleteAllRecentSearchAir() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        mockMvc.perform(delete(PrefixUrl+"/location/{location}", Location.AIR)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchPackageKeyword() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("캄보디아")
                        .name("4911,앙코르와트")
                        .build());
        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.PACKAGE, SearchType.KEYWORD.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchPackageCity() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .nationCode("JP")
                        .keyword("오사카, 일본")
                        .id("12495")
                        .build());
        // recentSearch.getRecentSearch().setKeyword("서귀포시");
        // recentSearch.getRecentSearch().setId("15754");
        // recentSearch.getRecentSearch().setId("8176");

        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.PACKAGE, SearchType.CITY.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchPackageProduct() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("[미서부 8일] 7대캐년+라스베가스 특급호텔 2박+데스밸리_아시아나항공")
                        .id("16523-89110")
                        .build());
        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.PACKAGE, SearchType.PRODUCT.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void setRecentSearchPackageArea() throws Exception {
        recentSearchDto.setRecentSearch(
                RecentSearch.builder()
                        .keyword("도쿄")
                        .data("100276,12287,100285")
                        .build());
        System.out.println(JsonUtils.toMapperPretty(recentSearchDto));

        mockMvc.perform(post(PrefixUrl+"/location/{location}/search_type/{search_type}", Location.PACKAGE, SearchType.AREA.name())
                        .content(JsonUtils.toMapper(recentSearchDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRecentSearchPackage() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        ResultActions result = mockMvc.perform(get(PrefixUrl+"/location/{location}", Location.PACKAGE)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print());
        String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<RecentSearch> recentSearchList = JsonUtils.toMapperObject(responseBody, new TypeReference<>() {});
        recentSearchList.forEach(it -> System.out.println(JsonUtils.toMapperPretty(it)));
    }

    @Test
    @Description("전체 삭제")
    void deleteAllRecentSearchPackage() throws Exception {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("idvisitor", recentSearchDto.getIdvisitor());
        paramMap.add("member_no", recentSearchDto.getMemberNo());
        mockMvc.perform(delete(PrefixUrl+"/location/{location}", Location.PACKAGE)
                        .params(paramMap))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }
}
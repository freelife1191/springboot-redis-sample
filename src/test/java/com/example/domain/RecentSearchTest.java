package com.example.domain;

import com.example.domain.recent.RecentSearch;
import org.junit.jupiter.api.Test;

/**
 * Created by mskwon on 2/20/24.
 */
class RecentSearchTest {

    @Test
    void convertData() {
        String data = "{\"ICN\":\"서울(인천)\",\"NRT\":\"도쿄(나리타)\",\"ROM\":\"로마\",\"PAR\":\"파리\",\"BCN\":\"바르셀로나\"}";
        String convertData = RecentSearch.convertData(data);
        System.out.println(convertData);
    }

    @Test
    void parseData() {
        String data = "ICN=서울(인천)⎮NRT=도쿄(나리타)⎮ROM=로마⎮PAR=파리⎮BCN=바르셀로나";
        String parseData = RecentSearch.parseData(data);
        System.out.println(parseData);
    }

    @Test
    void arrayParseDataTest() {
        String data = "1234,5678,9999";
        String parseData = RecentSearch.parseData(data);
        System.out.println(parseData);
    }
}
package com.example.core;

import com.example.core.annotation.UnitTest;
import com.example.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@ActiveProfiles("test")
@UnitTest
public class BaseBasicTest {

    protected StopWatch stopWatch; //개별 테스트 성능 측정용
    protected static StopWatch totalStopWatch; //종합 테스트 성능 측정용

    protected static File readFile(Path path, String filename) {
        if (path == null) {
            throw new RuntimeException("is Required path!!");
        }
        if (StringUtils.isEmpty(filename)) {
            throw new RuntimeException("is Required name!!");
        }

        return new File(Paths.get("src","test","resources", path.toString(), filename).toString());
    }

    protected static String getJson(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new RuntimeException("is Required name!!");
        }

        String result;

        File jsonData = readFile(Paths.get("json"), name+".json");
        try {
            Map<String, Object> jsonMap = JsonUtils.getObjectMapper().readValue(jsonData, new TypeReference<>() {});
            result = JsonUtils.getObjectMapper().writeValueAsString(jsonMap);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return result;
    }
}

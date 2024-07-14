package com.example.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by mskwon on 2023/12/12.
 */
public class FileUtils {

    public static String getJson(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new RuntimeException("is Required name!!");
        }

        String result;

        // Path filePath = Paths.get(File.separatorChar + "xml", File.separatorChar + fileName);
        // File xmlFile = new File(Paths.get("src","main","resources", filePath.toString(), fileName).toString());
        /*
        ClassPathResource resource = new ClassPathResource("json/" + fileName);

        StringBuilder result = new StringBuilder();
        try {
            for (String data : Files.readLines(resource.getFile(), Charset.defaultCharset())) {
                result.append(data).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
        File jsonData;
        ClassPathResource resource = new ClassPathResource("json/" + name+".json");
        try {
            jsonData = resource.getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Map<String, Object> jsonMap = JsonUtils.getObjectMapper().readValue(jsonData, new TypeReference<>() {});
            result = JsonUtils.getObjectMapper().writeValueAsString(jsonMap);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return result;
    }
}

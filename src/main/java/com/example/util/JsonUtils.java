/**
 * JsonUtils.java $version 2017. 7. 18.
 * <p>
 * Copyright 2017 Foodtechkorea Corp. All rights Reserved.
 * FOODTECHKOREA PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.example.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * The type Json utils.
 *
 * @author Foodtech Korea, bhkwon@foodtechkorea.com
 * @since 2017. 7. 18.
 */
public class JsonUtils {

    private final ObjectMapper mapper;

    private JsonUtils() {
        mapper = new ObjectMapper();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    private static JsonUtils getInstance() {
        return new JsonUtils();
    }

    public static ObjectMapper getObjectMapper() {
        // JavaTimeModule javaTimeModule = new JavaTimeModule();
        // javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        // JsonFormat.Value format = JsonFormat.Value.forPattern("yyyy-MM-dd");
        // getInstance().mapper.configOverride(LocalDate.class).setFormat(format);
        return getInstance().mapper
                // .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                // .registerModule(javaTimeModule)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * To ObjectMapper pretty json string
     * @param object
     * @return
     */
    public static String toMapper(Object object){
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * To ObjectMapper pretty json string
     * @param object
     * @return
     */
    public static String toMapperPretty(Object object){
        try {
            return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * To ObjectMapper Mapping json string
     * @param obj JSON String
     * @param clz 변환할 오브젝트 클래스
     * @param <T>
     * @return
     */
    public static<T> T toMapperObject(String obj, Class<T> clz) {
        if(StringUtils.isEmpty(obj)) return null;
        try {
            return getObjectMapper().readValue(obj, clz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * To ObjectMapper Mapping json string
     * @param obj JSON String
     * @param type 변환할 오브젝트 클래스
     * @param <T>
     * List 와 같은 형태는 TypeReference 형으로 처리
     * new TypeReference<List<Object>() {}
     * @return
     */
    public static<T> T toMapperObject(String obj, TypeReference<T> type) {
        if(StringUtils.isEmpty(obj)) return (T) new TypeReference<Object>(){};
        try {
            return getObjectMapper().readValue(obj, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String toJsonString(T obj, boolean isPretty) {
        Jackson2ObjectMapperBuilder b = new Jackson2ObjectMapperBuilder();
        b.propertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        b.dateFormat(df);
        b.serializationInclusion(JsonInclude.Include.NON_NULL);

        ObjectMapper mapper = b.build();

        try {
            if (isPretty) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            } else {
                return mapper.writeValueAsString(obj);
            }
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

}
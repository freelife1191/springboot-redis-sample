package com.example.util;

import com.example.domain.recent.Site;
import com.example.exception.ParameterException;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Created by mskwon on 3/29/24.
 * 유효성 검사 유틸리티
 */
public class ValidationUtil {

    /**
     * memberNo, idvisitor 필수 파라미터 유효성 검사
     * @param memberNo
     * @param idvisitor
     * @return
     */
    public static Map<String, String> verifyRequiredIdKey(String memberNo, String idvisitor) {
        Map<String, String> errorMap = new LinkedHashMap<>();

        if (isBlank(memberNo) && isBlank(idvisitor))
            errorMap.put("member_no, idvisitor", "Must provide member_no or idvisitor.");

        if (!errorMap.keySet().isEmpty()) throw new ParameterException(errorMap);
        return errorMap;
    }

    /**
     * idvisitor 값을 유효하지 않은 값
     * @param errorMap
     * @param idvisitor
     * @return
     */
    public static Map<String, String> verifyIdvisitor(Map<String, String> errorMap, String idvisitor) {
        if (StringUtils.isBlank(idvisitor)) return errorMap;
        if (errorMap.isEmpty()) errorMap = new LinkedHashMap<>();
        String idvisitorValidPattern = "^\\w+$";
        if (!idvisitor.matches(idvisitorValidPattern) && idvisitor.length() > 20)
            errorMap.put("idvisitor", "Invalid 'idvisitor' format.");
        return errorMap;
    }

    /**
     * 필수값 검증
     * @param errorMap
     * @param property
     * @param propertyName
     */
    public static  <T extends Enum<T>> void  verifyRequired(Map<String, String> errorMap, Enum<T> property, String propertyName) {
            verifyRequired(errorMap, property != null ? property.name() : null, propertyName);
        }

    public static void verifyRequired(Map<String, String> errorMap, String property, String propertyName) {
        String errorMessage = "The '%s' should be provided.";
        if (isBlank(property) || property.contains("undefined")) errorMap.put(propertyName, errorMessage.formatted(propertyName));
    }

    public static void verifyParseLocalDate(Map<String, String> errorMap, String property, String propertyName) {
        if (isBlank(property) || property.contains("undefined")) return;
        try {
            LocalDate.parse(property, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException | SerializationException e) {
            String errorMessage = "The '%s' date format is incorrect. Input Value: %s, Date format: yyyy-MM-dd e.g.) 2024-01-01";
            errorMap.put(propertyName, errorMessage.formatted(propertyName, property));
        }
    }

    /**
     * 숫자 타입인지 검증 후 숫자타입으로 변환
     * @param errorMap
     * @param property
     * @param propertyName
     * @return
     */
    public static Integer parseInt(Map<String, String> errorMap, String property, String propertyName) {
        if (!isNumeric(property)) {
            errorMap.put(propertyName, "'%s' must be of numeric type.".formatted(propertyName));
            return null;
        }
        return Integer.valueOf(property);
    }
}

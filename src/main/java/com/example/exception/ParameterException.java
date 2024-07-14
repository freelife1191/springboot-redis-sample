package com.example.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Created by mskwon on 2023/12/28.
 */
@Getter
public class ParameterException extends RuntimeException {

    private Map<String, String> errorMap;

    public ParameterException(String message) {
        super(message);
    }

    public ParameterException(Map<String, String> errorMap) {
        this.errorMap = errorMap;
    }
}

package com.example.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchFile {
    CITY("searchCity"),
    HOTEL("searchHotel");

    private final String filename;

    public String getName() {
        return this.filename;
    }
}
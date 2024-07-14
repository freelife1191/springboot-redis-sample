package com.example.adapter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Created by mskwon on 2023/12/15.
 */
@Slf4j
public class RecentSearchValueSerializer implements RedisSerializer<RecentSearchValue> {
    @Override
    public byte[] serialize(RecentSearchValue recentSearchValue) throws SerializationException {
        if (Objects.isNull(recentSearchValue)) {
            return null;
        }

        StringJoiner value = new StringJoiner("|");
        value.add(recentSearchValue.getVersion());
        value.add(replaceSeparator(recentSearchValue.getNationCode()));
        value.add(recentSearchValue.getSearchType());
        value.add(replaceSeparator(recentSearchValue.getDivision()));
        value.add(recentSearchValue.getKeyword());
        value.add(recentSearchValue.getId());
        value.add(replaceSeparator(recentSearchValue.getOptions()));
        value.add(recentSearchValue.getFrom() != null ? recentSearchValue.getFrom().toString() : "");
        value.add(recentSearchValue.getTo() != null ? recentSearchValue.getTo().toString() : "");
        value.add(replaceSeparator(recentSearchValue.getCode()));
        value.add(replaceSeparator(recentSearchValue.getData()));
        value.add(StringUtils.defaultIfEmpty(recentSearchValue.getName(), ""));
        value.add(StringUtils.defaultIfEmpty(recentSearchValue.getParent(), ""));
        value.add(StringUtils.defaultIfEmpty(recentSearchValue.getType(), ""));
        value.add(StringUtils.defaultIfEmpty(recentSearchValue.getEtc(), ""));
        // log.debug("recent_search_value serialize: {}", value);
        return value.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public RecentSearchValue deserialize(byte[] bytes) throws SerializationException, DateTimeParseException {
        if (Objects.isNull(bytes)) {
            return null;
        }

        String[] splitValue = new String(bytes, StandardCharsets.UTF_8).split("\\|");

        int i = 0;
        String version = setValue(splitValue, i++); // 0
        String nationCode = setValue(splitValue, i++); // 1
        String searchType = setValue(splitValue, i++); // 2
        String division = setValue(splitValue, i++); // 3
        String keyword = setValue(splitValue, i++); // 4
        String id = setValue(splitValue, i++); // 5
        String options = setValue(splitValue, i++); // 7
        LocalDate from = parseLocalDate(splitValue, i); // 8
        i++;
        LocalDate to = parseLocalDate(splitValue, i); // 9
        i++;
        String code = setValue(splitValue, i++); // 10
        String data = setValue(splitValue, i++); // 11
        String name = setValue(splitValue, i++); // 12
        String parent = setValue(splitValue, i++); // 13
        String type = setValue(splitValue, i++); // 14
        String etc = setValue(splitValue, i++); // 15
        RecentSearchValue recentSearchValue = RecentSearchValue.from(version, nationCode, searchType, division, keyword, id, options, from, to, code, data, name, parent, type, etc);
        // log.debug("recent_search_value deserialize: {}", recentSearchValue);
        return recentSearchValue;
    }

    private LocalDate parseLocalDate(String[] splitValue, int i) throws DateTimeParseException {
        return StringUtils.isNotEmpty(setValue(splitValue, i)) ? LocalDate.parse(setValue(splitValue, i), DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
    }

    private String setValue(String[] splitValue, int num) {
        if (splitValue.length-1 < num) return "";
        return reverseSeparator(splitValue[num]);
    }

    private String reverseSeparator(String value) {
        return replaceSeparator(value, true);
    }

    private String replaceSeparator(String value) {
        return replaceSeparator(value, false);
    }
    private String replaceSeparator(String value, boolean reverse) {
        String regex = reverse ? "⎮" : "\\|";
        String replacement = reverse ? "\\|" : "⎮";
        return StringUtils.defaultIfEmpty(value, "").replaceAll(regex, replacement);
    }
}

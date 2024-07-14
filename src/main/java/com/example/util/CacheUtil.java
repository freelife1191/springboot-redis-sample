package com.example.util;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Created by mskwon on 2024/02/02.
 */
public class CacheUtil {


    /**
     * TTL을 문자열로 변환
     * @param ttl 만료까지 남은 시간(초)
     * @return 만료까지 남은 시간 문자열
     */
    public static String ttlToString(Long ttl) {
        return ttlToString(ttl, null);
    }

    /**
     * TTL을 문자열로 변환
     * @param ttl 만료까지 남은 시간(초)
     * @param display 표시 형식(s(초), m(분), h(시), d(일), all(일, 시, 분, 초), null 이면 자동
     * @return 만료까지 남은 시간 문자열
     */
    public static String ttlToString(Long ttl, String display) {
        if (ttl == null || ttl < 1L) return "0s";

        long second = ttl;
        long minute = second / 60;
        long hour = minute / 60;
        long day = hour / 24;

        String secondStr = second + "s";
        String minuteStr = minute + "m";
        String hourStr = hour + "h";
        String dayStr = day + "d";

        display = StringUtils.defaultIfBlank(display, "");
        return switch (display) {
            case "s" -> secondStr;
            case "m" -> minuteStr;
            case "h" -> hourStr;
            case "d" -> dayStr;
            case "all" -> {
                StringBuilder _ttl = new StringBuilder();
                if (day > 0) _ttl.append(day).append("d ");
                if (hour > 0) _ttl.append(hour % 24).append("h ");
                if (minute > 0) _ttl.append(minute % 60).append("m ");
                _ttl.append(second % 60).append("s");
                yield _ttl.toString();
            }
            default -> {
                String _ttl;
                if (day > 0) _ttl = dayStr;
                else if (hour > 0) _ttl = hourStr;
                else if (minute > 0) _ttl = minuteStr;
                else _ttl = secondStr;
                yield _ttl;
            }
        };
    }

    /**
     * LocalDateTime 비교값으로 현재시간과 비교해 TTL을 계산하여 응답
     * @param expiredAt 비교할 LocalDateTime
     * @return Second 단위의 TTL 값
     */
    public static Long toTtl(LocalDateTime expiredAt) {
        LocalDateTime nowAt = LocalDateTime.now();
        long nowSecond = Timestamp.valueOf(nowAt).getTime() / 1000;
        long expiredSecond = Timestamp.valueOf(expiredAt).getTime() / 1000;
        long ttl = expiredSecond - nowSecond;
        return ttl < 1L ? 0 : ttl;
    }

    public static String toTtlString(LocalDateTime updatedAt) {
        return ttlToString(toTtl(updatedAt));
    }
}

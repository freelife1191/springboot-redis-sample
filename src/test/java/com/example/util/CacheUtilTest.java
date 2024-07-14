package com.example.util;

import org.junit.jupiter.api.Test;

/**
 * Created by mskwon on 2024/02/02.
 */
class CacheUtilTest {

    @Test
    void ttlToStringTest() {
        System.out.println(CacheUtil.ttlToString(90061L, "all"));
        System.out.println(CacheUtil.ttlToString(90061L, "d"));
        System.out.println(CacheUtil.ttlToString(90061L, "h"));
        System.out.println(CacheUtil.ttlToString(90061L, "m"));
        System.out.println(CacheUtil.ttlToString(90061L, "s"));
        System.out.println(CacheUtil.ttlToString(null));
    }
}
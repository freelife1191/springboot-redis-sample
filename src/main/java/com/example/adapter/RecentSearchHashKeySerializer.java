package com.example.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Created by mskwon on 2023/12/15.
 */
@Slf4j
public class RecentSearchHashKeySerializer implements RedisSerializer<RecentSearchHashKey> {

    @Override
    public byte[] serialize(RecentSearchHashKey recentSearchHashKey) throws SerializationException {
        if (Objects.isNull(recentSearchHashKey))
            throw new SerializationException("recentSearchHashKey is null");
        // log.debug("recent_search_hashkey serialize: {}", recentSearchHashKey);
        return recentSearchHashKey.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public RecentSearchHashKey deserialize(byte[] bytes) throws SerializationException {
        if (Objects.isNull(bytes))
            throw new SerializationException("bytes is null");
        RecentSearchHashKey hashKey = RecentSearchHashKey.fromString(new String(bytes, StandardCharsets.UTF_8));
        // log.debug("recent_search_hashkey deserialize: {}", hashKey);
        return hashKey;
    }

}

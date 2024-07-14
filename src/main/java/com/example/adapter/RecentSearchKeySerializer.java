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
public class RecentSearchKeySerializer implements RedisSerializer<RecentSearchKey> {

    @Override
    public byte[] serialize(RecentSearchKey recentSearchKey) throws SerializationException {
        if (Objects.isNull(recentSearchKey))
            throw new SerializationException("recentSearchKey is null");
        // log.debug("recent_search_key serialize: {}", recentSearchKey);
        return recentSearchKey.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public RecentSearchKey deserialize(byte[] bytes) throws SerializationException {
        if (Objects.isNull(bytes))
            throw new SerializationException("bytes is null");
        RecentSearchKey key = RecentSearchKey.fromString(new String(bytes, StandardCharsets.UTF_8));
        // log.debug("recent_search_key deserialize: {}", key);
        return key;
    }

}

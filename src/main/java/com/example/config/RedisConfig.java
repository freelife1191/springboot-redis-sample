package com.example.config;

import com.example.adapter.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Bean("recentSearchZSetRedisTemplate")
    public RedisTemplate<RecentSearchKey, RecentSearchHashKey> recentSearchZSetRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<RecentSearchKey, RecentSearchHashKey> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new RecentSearchKeySerializer());
        redisTemplate.setValueSerializer(new RecentSearchHashKeySerializer());
        return redisTemplate;
    }

    @Bean("recentSearchHashRedisTemplate")
    public RedisTemplate<RecentSearchKey, RecentSearchValue> recentSearchHashRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<RecentSearchKey, RecentSearchValue> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new RecentSearchKeySerializer());
        redisTemplate.setHashKeySerializer(new RecentSearchHashKeySerializer());
        redisTemplate.setHashValueSerializer(new RecentSearchValueSerializer());
        return redisTemplate;
    }

}
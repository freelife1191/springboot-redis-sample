package com.example.adapter;

import com.example.config.RecentConfig;
import com.example.domain.recent.IdType;
import com.example.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mskwon on 2023/12/15.
 */
@Slf4j
@Component
public class RecentSearchCacheAdapter {
    private final RedisTemplate<RecentSearchKey, RecentSearchHashKey> recentSearchZSetRedisTemplate;
    private final RedisTemplate<RecentSearchKey, RecentSearchValue> recentSearchHashRedisTemplate;
    private final ZSetOperations<RecentSearchKey, RecentSearchHashKey> zSetOperation;
    private final HashOperations<RecentSearchKey, RecentSearchHashKey, RecentSearchValue> hashOperation;
    private final RedisOperations<RecentSearchKey, RecentSearchValue> redisOperations;
    private final RecentConfig recentConfig;

    public RecentSearchCacheAdapter(RedisTemplate<RecentSearchKey, RecentSearchHashKey> recentSearchZSetRedisTemplate,
                                    RedisTemplate<RecentSearchKey, RecentSearchValue> recentSearchHashRedisTemplate,
                                    RecentConfig recentConfig) {
        this.recentSearchZSetRedisTemplate = recentSearchZSetRedisTemplate;
        this.recentSearchHashRedisTemplate = recentSearchHashRedisTemplate;
        this.zSetOperation = recentSearchZSetRedisTemplate.opsForZSet();
        this.hashOperation = recentSearchHashRedisTemplate.opsForHash();
        this.redisOperations = recentSearchHashRedisTemplate;
        this.recentConfig = recentConfig;
    }

    public void put(RecentSearchKey key, RecentSearchHashKey hashKey, RecentSearchValue value) {
        put(key, hashKey, value, LocalDateTime.now());
    }
    public void put(RecentSearchKey key, RecentSearchHashKey hashKey, RecentSearchValue value, LocalDateTime localDateTime) {
        if (!recentConfig.getSearch().isWrite()) {
            log.info("Redis:: RecentSearch Write: {}", false);
            return;
        }

        Duration expire = getExpire(key);

        log.debug("Redis:: RecentSearch Put key: {}, hashKey: {}, value: {}, expire: {}", key, hashKey, value, expire);

        zSetOperation.add(key, hashKey, Timestamp.valueOf(localDateTime).getTime());
        zSetOperation.getOperations().expire(key, expire);
        hashOperation.put(key.hash(), hashKey, value);
        hashOperation.getOperations().expire(key.hash(), expire);
        if (Objects.requireNonNull(zSetOperation.size(key)) > recentConfig.getSearch().getMaxSize() ) {
            // ZSet에 저장된 가장오래된 최근검색 Cache 값
            // log.debug("delete: {}",zSetOperation.rangeWithScores(key, 0, 0));
            // ZSet에 저장된 가장오래된 최근검색 Cache Key 삭제
            TypedTuple<RecentSearchHashKey> popMin = zSetOperation.popMin(key); // [DefaultTypedTuple [score=1.702617471153E12, value=3_1552577]]
            // Hash에 저장된 가장오래된 최근검색 Cache Value 삭제
            if (popMin != null) hashOperation.delete(key.hash(), popMin.getValue()); // DefaultTypedTuple [score=1.702617471153E12, value=3_1552577]
        }
    }

    public boolean isNotEmpty(RecentSearchKey key, RecentSearchHashKey hashKey) {
        if (hashKey == null) {
            Long size = zSetOperation.size(key);
            return size != null && size > 0;
        }
        try {
            return hashOperation.get(key.hash(), hashKey) != null;
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    public boolean isNotEmpty(RecentSearchKey key) {
        return isNotEmpty(key, null);
    }

    public boolean isEmpty(RecentSearchKey key, RecentSearchHashKey hashKey) {
        return !isNotEmpty(key, hashKey);
    }

    public boolean isEmpty(RecentSearchKey key) {
        return !isNotEmpty(key);
    }

    public List<RecentSearchValue> get(RecentSearchKey key, LocalDateTime now) {
        List<RecentSearchHashKey> hashKeys = Objects.requireNonNull(zSetOperation.reverseRangeWithScores(key, 0, recentConfig.getSearch().getMaxSize()-1)).stream()
                .filter(Objects::nonNull)
                .map(RecentSearchHashKey::to)
                .toList();
        Long ttl = redisOperations.getExpire(key);
        return hashKeys.stream()
                .map(hashKey -> getRecentSearchValue(key, hashKey, ttl, now))
                .filter(Objects::nonNull)
                .toList();
    }

    public List<RecentSearchValue> get(List<RecentSearchKey> keys) {
        LocalDateTime now = LocalDateTime.now();
        List<RecentSearchHashKey> hashKeys = List.copyOf(keys.stream()
                .map(key -> zSetOperation.reverseRangeWithScores(key, 0, recentConfig.getSearch().getMaxSize()-1))
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .map(RecentSearchHashKey::to)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return List.copyOf(keys.stream()
                .map(key -> {
                    Long ttl = redisOperations.getExpire(key);
                    return hashKeys.stream()
                            .map(hashKey -> getRecentSearchValue(key, hashKey, ttl, now))
                            .filter(Objects::nonNull)
                            .toList();
                })
                .flatMap(List::stream)
                .sorted(Comparator.comparing(RecentSearchValue::getUpdatedAt).reversed().thenComparing(RecentSearchValue::getIdType))
                .filter(CollectionUtil.distinctByKey(RecentSearchValue::getId))
                .limit(recentConfig.getSearch().getMaxSize()) // merge 후 maxSize 만큼만 가져옴
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private RecentSearchValue getRecentSearchValue(RecentSearchKey key, RecentSearchHashKey hashKey, Long ttl, LocalDateTime now) {
        try {
            RecentSearchValue value = hashOperation.get(key.hash(), hashKey);
            return value != null ? value.setIdType(key.getIdType()).setTime(hashKey.getScore(), ttl, now) : null;
        } catch (DateTimeParseException | SerializationException e) {
            int delete = delete(key, hashKey);
            log.warn("recentSearchValue parse error:: {}, delete: {}, key: {}, hashkey: {}", e.getMessage(), delete, key, hashKey);
            return null;
        }
    }

    public RecentSearchValue get(RecentSearchKey key, RecentSearchHashKey hashKey, LocalDateTime now) {
        Long ttl = redisOperations.getExpire(key);
        RecentSearchValue value = hashOperation.get(key.hash(), hashKey);
        Double score = zSetOperation.score(key, hashKey);
        if (score == null) return null;
        return value != null ? value.setIdType(key.getIdType()).setTime(score.longValue(), ttl, now) : null;
    }

    private Duration getExpire(RecentSearchKey key) {
        return switch (IdType.valueOf(key.getIdType())) {
            case V -> recentConfig.getSearch().getIdvisitorExpire();
            case M -> recentConfig.getSearch().getMemberExpire();
        };
    }

    public int delete(RecentSearchKey key, RecentSearchHashKey hashKey) {
        if (!recentConfig.getSearch().isDelete()) {
            log.info("Redis:: RecentSearch Delete: {}", false);
            return 0;
        }
        if (StringUtils.isNotBlank(hashKey.toString())) {
            log.debug("Redis:: RecentSearch Delete key: {}, hashkey: {}", key, hashKey);
            if (isEmpty(key, hashKey)) return 0;
            zSetOperation.remove(key, hashKey); // zSet 정렬데이터 삭제
            Long delete = hashOperation.delete(key.hash(), hashKey); // hash 데이터 삭제
            return delete.intValue();
        }
        log.debug("Redis:: RecentSearch Delete key: {}", key);
        if (isEmpty(key)) return 0;
        redisOperations.unlink(key);
        Long unlink = redisOperations.unlink(List.of(key.hash()));
        unlink = unlink != null ? unlink : 0;
        return unlink.intValue();
    }

}

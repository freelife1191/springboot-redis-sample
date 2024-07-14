package com.example.adapter;

import com.example.dto.recent.RecentSearchDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Created by mskwon on 2023/12/15.
 */
@Getter
public class RecentSearchHashKey {

    private final String searchType; // 검색타입
    private final String searchId; // 검색 ID

    @Hidden
    @JsonIgnore
    private long score;

    @Builder
    public RecentSearchHashKey(String searchType, String searchId) {
        this.searchType = searchType;
        this.searchId = searchId;
    }

    public static RecentSearchHashKey to(String searchType, String keywordId) {
        return new RecentSearchHashKey(searchType, keywordId);
    }

    public static RecentSearchHashKey to(RecentSearchDto dto) {
        return to(dto.getRecentSearch().getSearchType().toString(), dto.getRecentSearch().getId());
    }

    public static RecentSearchHashKey to(RecentSearchValue value) {
        return to(value.getSearchType(), value.getId());
    }

    public static RecentSearchHashKey fromString(String key) {
        String[] splitValue = String.valueOf(key).split("_");
        return RecentSearchHashKey.to(splitValue[0], splitValue[1]);
    }

    public String toString() {
        if (StringUtils.isBlank(this.searchType) || StringUtils.isBlank(this.searchId))
            return "";
        StringJoiner key = new StringJoiner("_");
        key.add(this.searchType);
        key.add(this.searchId);
        return key.toString();
    }

    public static RecentSearchHashKey to(TypedTuple<RecentSearchHashKey> typedTupleHashKey) {
        RecentSearchHashKey hashKey = typedTupleHashKey.getValue();
        assert hashKey != null;
        hashKey.score = Objects.requireNonNull(typedTupleHashKey.getScore()).longValue();
        return hashKey;
    }
}

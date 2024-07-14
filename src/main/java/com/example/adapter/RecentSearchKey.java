package com.example.adapter;

import com.example.domain.recent.IdType;
import com.example.dto.recent.RecentSearchDto;
import com.example.exception.ParameterException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by mskwon on 2023/12/15.
 */
@Builder
public class RecentSearchKey {

    private static final String PREFIX = "TRAVEL";
    private static final String SERVICE = "RECENT";
    private static final String SUFFIX = "HASH";
    /* hash true: hash 타입으로 저장, false: sortedSet 타입으로 저장 */
    private boolean hash;
    @Getter
    private String site; // 사이트
    private String location; // 카테고리(검색위치)
    @Setter
    @Getter
    private String idType; // 회원구분(M:회원, V:비회원)
    @Getter
    private String idValue; // 회원 ID

    private RecentSearchKey(boolean hash, String site, String location, String idType, String idValue) {
        if (StringUtils.isEmpty(site)) throw new ParameterException("site can't be null");
        else if (StringUtils.isEmpty(location)) throw new ParameterException("location can't be null");
        else if (StringUtils.isEmpty(idType)) throw new ParameterException("idType can't be null");
        else if (StringUtils.isEmpty(idValue)) throw new ParameterException("idValue can't be null");
        this.hash = hash;
        this.site = site;
        this.location = location;
        this.idType = idType;
        this.idValue = idValue;
    }

    public static RecentSearchKey to(boolean hash, String site, String location, String idType, String idValue) {
        return new RecentSearchKey(hash, site, location, idType, idValue);
    }

    public static RecentSearchKey to(String site, String location, String idType, String idValue) {
        return new RecentSearchKey(false, site, location, idType, idValue);
    }

    public static List<RecentSearchKey> to(RecentSearchDto dto) {
        List<RecentSearchKey> keys = new ArrayList<>();
        if (StringUtils.isNotEmpty(dto.getIdvisitor()))
            keys.add(to(dto.getSite().name(), dto.getLocation().name(), IdType.V.name(), dto.getIdvisitor()));
        if (StringUtils.isNotEmpty(dto.getMemberNo()))
            keys.add(to(dto.getSite().name(), dto.getLocation().name(), IdType.M.name(), dto.getMemberNo()));
        return keys;
    }

    public static RecentSearchKey to(RecentSearchDto dto, IdType idType, String idValue) {
        if (idValue == null) return null;
        return to(dto.getSite().name(), dto.getLocation().name().toLowerCase(), idType.name(), idValue);
    }

    public RecentSearchKey hash() {
        return RecentSearchKey.to(true, this.site, this.location, this.idType, this.idValue);
    }

    public static RecentSearchKey fromString(String key) {
        String[] splitValue = String.valueOf(key).split("\\|");

        return RecentSearchKey.builder()
                .site(splitValue[1])
                .location(splitValue[2])
                .idType(splitValue[3])
                .idValue(splitValue[4])
                .build();
    }

    public String toString() {
        StringJoiner key = new StringJoiner(":");
        key.add(PREFIX);
        key.add(SERVICE);
        key.add(this.site);
        key.add(this.location);
        key.add(this.idType);
        key.add(this.idValue);
        if (hash) key.add(SUFFIX);

        return key.toString();
    }

    public static RecentSearchKey recentSearchKeyByIdType(List<RecentSearchKey> keys, IdType idType) {
        return keys.stream()
                .filter(key -> key.getIdType().equals(idType.name()))
                .findFirst().orElse(null);
    }
}

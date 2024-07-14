package com.example.service.recent;

import com.example.adapter.RecentSearchCacheAdapter;
import com.example.adapter.RecentSearchHashKey;
import com.example.adapter.RecentSearchKey;
import com.example.adapter.RecentSearchValue;
import com.example.domain.recent.IdType;
import com.example.domain.recent.RecentSearch;
import com.example.domain.recent.SearchType;
import com.example.domain.recent.Site;
import com.example.dto.recent.RecentSearchDto;
import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by mskwon on 2023/12/15.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecentSearchService {

    private final RecentSearchCacheAdapter cacheAdapter;
    private ExecutorService es;

    @PostConstruct
    private void init() {
        es = Executors.newCachedThreadPool();
    }

    public int delete(Site site, String location, String memberNo, String idvisitor, SearchType searchType, String id) {
        RecentSearchDto dto = RecentSearchDto.from(HttpMethod.DELETE, location, memberNo, idvisitor, site, searchType, id);
        String _searchType = dto.getSearchType() != null ? dto.getSearchType().toString() : null;
        String _keywordId = dto.getSearchType() != null ? dto.getRecentSearch().getId() : null;
        RecentSearchHashKey hashKey = RecentSearchHashKey.to(_searchType, _keywordId);
        List<RecentSearchKey> keys = RecentSearchKey.to(dto);
        int deleteCount = keys.stream()
                .filter(Objects::nonNull)
                .map(key -> cacheAdapter.delete(key, hashKey))
                .reduce(0, Integer::sum);
        if (deleteCount < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT, HttpStatus.NO_CONTENT.getReasonPhrase());
        return deleteCount;
    }

    public List<RecentSearch> get(Site site, String location, String memberNo, String idvisitor) {
        List<RecentSearchKey> keys = RecentSearchKey.to(RecentSearchDto.from(HttpMethod.GET, location, memberNo, idvisitor, site)).stream().toList();
        return getValuesWithCleansing(site.name(), keys).stream()
                .map(value -> RecentSearch.to(value, location))
                .toList();
    }

    public void set(RecentSearchDto dto) {
        dto.init();
        setRecentSearchByType(dto);
        RecentSearchHashKey hashKey = RecentSearchHashKey.to(dto);
        log.debug("dto: {}",dto);
        RecentSearchValue value = RecentSearchValue.to(dto);

        List<RecentSearchKey> keys = RecentSearchKey.to(dto);
        RecentSearchKey memberNoKey = RecentSearchKey.recentSearchKeyByIdType(keys, IdType.M);
        RecentSearchKey idvisitorKey = RecentSearchKey.recentSearchKeyByIdType(keys, IdType.V);

        // 조회 시 Cache Cleansing 처리를 하므로 등록시에는 필요없을 수 있음
        RecentSearchKey selectKey = memberNoKey != null ? memberNoKey : idvisitorKey;
        // 중복 키워드 캐시 삭제 비동기 처리
        CompletableFuture.runAsync(() -> deleteDuplicateKeyword(dto, selectKey), es);
        // 로그인 시 member_no에만 우선적으로 Cache 등록, 비로그인 시에는 idivisitor에 Cache 등록
        cacheAdapter.put(selectKey, hashKey, value);
        // Cache Cleansing 처리
        // CompletableFuture.runAsync(() -> allCacheCleansing(dto.getSite().name(), dto.getMember_no(), dto.getIdvisitor()), es);
    }

    /**
     * 특정 사이트의 회원 또는 브라우저ID의 모든 최근 검색 데이터 조회
     */
    public List<RecentSearch> getRecentSearches(Site site, String memberNo, String idvisitor) {

        if (StringUtils.isBlank(memberNo) && StringUtils.isBlank(idvisitor)) return Lists.newArrayList();

        List<RecentSearch> resultList = Lists.newArrayList();
        for (RecentSearch.Inventory inventory: RecentSearch.Inventory.values()) {
            List<RecentSearchKey> keys = RecentSearchKey.to(RecentSearchDto.from(HttpMethod.GET, inventory.getLocation().name().toLowerCase(), memberNo, idvisitor, site)).stream().toList();
            resultList.addAll(getValuesWithCleansing(site.name(), keys).stream()
                    .map(value -> RecentSearch.to(value, inventory.getLocation().name()))
                    .toList());
        }
        resultList.sort(Comparator.comparing(RecentSearch::getUpdatedAt));
        return resultList;
    }

    /**
     * 키워드가 같은 기존 캐시가 있으면 삭제
     */
    private void deleteDuplicateKeyword(RecentSearchDto dto, RecentSearchKey selectKey) {
        LocalDateTime now = LocalDateTime.now();
        List<RecentSearchValue> values = cacheAdapter.get(selectKey, now).stream()
                .filter(it -> SearchType.valueOf(it.getSearchType()) != dto.getSearchType())
                .filter(it -> it.getKeyword().hashCode() == dto.getRecentSearch().getKeyword().hashCode())
                .toList();
        if (!values.isEmpty()) {
            values.forEach(it -> {
                log.debug("deleteDuplicateKeyword: {}", it);
                cacheAdapter.delete(selectKey, RecentSearchHashKey.to(it));
            });
        }
    }

    /**
     * Cache 정리(member_no가 있을 시 idvisitor Cache 삭제)
     * idvisitor Cache를 정리했으면 병합된 데이터를 반환
     * idivisitor가 있으면 member_no와 병합된 데이터를 put 하고 idvisitor Cache 삭제
     */
    private List<RecentSearchValue> getValuesWithCleansing(String site, List<RecentSearchKey> keys) {
        RecentSearchKey memberNoKey = RecentSearchKey.recentSearchKeyByIdType(keys, IdType.M);
        RecentSearchKey idvisitorKey = RecentSearchKey.recentSearchKeyByIdType(keys, IdType.V);

        String memberNo = memberNoKey != null ? memberNoKey.getIdValue() : null;
        String idvisitor = idvisitorKey != null ? idvisitorKey.getIdValue() : null;

        log.debug("getValuesWithCleansing: {}, {}, {}", site, memberNo, idvisitor);

        // Cache Cleansing 비동기 처리
        CompletableFuture.runAsync(() -> allCacheCleansing(site, memberNo, idvisitor), es);
        return cacheAdapter.get(keys);
    }

    /**
     * member_no 로그인시 idvisitor 모든 Cache cleansing 처리
     * @param site 사이트
     * @param memberNo 회원ID
     * @param idvisitor 비회원ID
     */
    private void allCacheCleansing(String site, String memberNo, String idvisitor) {
        if (StringUtils.isBlank(memberNo) || StringUtils.isBlank(idvisitor)) return;
        long start = System.currentTimeMillis();
        log.debug("allCacheCleansing start: {}, {}, {}", site, memberNo, idvisitor);
        AtomicInteger updateCount = new AtomicInteger();
        LocalDateTime now  = LocalDateTime.now();
        for (RecentSearch.Inventory inventory: RecentSearch.Inventory.values()) {
            RecentSearchKey idvisitorKey = RecentSearchKey.to(site, inventory.getLocation().name().toLowerCase(), IdType.V.name(), idvisitor);
            // log.debug("allCacheCleansing idvisitorKey: {}, isEmpty: {}",idvisitorKey, cacheAdapter.isEmpty(idvisitorKey));
            if (cacheAdapter.isEmpty(idvisitorKey)) continue; // merge 할 idvisitor 캐시가 없다면 PASS
            RecentSearchKey memberNoKey = RecentSearchKey.to(site, inventory.getLocation().name().toLowerCase(), IdType.M.name(), memberNo);
            // log.debug("allCacheCleansing memberNoKey: {}: isEmpty: {}",memberNoKey, cacheAdapter.isEmpty(memberNoKey));
            // if (cacheAdapter.isEmpty(memberNoKey)) continue;

            // 1. idvisitor 캐시 조회
            List<RecentSearchValue> idvisitorValues = cacheAdapter.get(idvisitorKey, now);
            // log.debug("allCacheCleansing cleansing - idvisitorKey: {}, size: {}", idvisitorKey, idvisitorValues.size());
            // 2. idvisitor 캐시 삭제
            // cacheAdapter.delete(idvisitorKey, RecentSearchHashKey.to(null, null));
            // 3. member_no에 idvisitor 캐시 병합
            idvisitorValues.forEach(idvisitorValue -> {
                // idvisitorKey와 동일한 memberNoKey 데이터 조회
                RecentSearchValue memberValue = cacheAdapter.get(memberNoKey, RecentSearchHashKey.to(idvisitorValue), now);
                // 동일한 memberNo 데이터가 있다면 idvisitor 데이터보다 이전 데이터인지 확인
                boolean before = memberValue != null && idvisitorValue.getUpdatedAt().isAfter(memberValue.getUpdatedAt());
                // idvisitor와 동일한 memberNo 데이터가 없으면 idvisitor 데이터 memberNo 최근 검색에 merge 처리
                // idvisitor 데이터보다 memberNo 데이터가 이전 데이터이면 idvisitor 최근 검색 조회 시간으로 업데이트
                if (cacheAdapter.isEmpty(memberNoKey, RecentSearchHashKey.to(idvisitorValue)) || before) {
                    cacheAdapter.put(memberNoKey, RecentSearchHashKey.to(idvisitorValue), idvisitorValue, idvisitorValue.getUpdatedAt());
                    updateCount.getAndIncrement();
                }
            });
        }
        long end = System.currentTimeMillis();
        log.debug("allCacheCleansing end: {}, {}, {}, updateCount: {}, execute time: {} ms", site, memberNo, idvisitor, updateCount, ( end - start )/1000.0);
    }

    /**
     * 최근검색 타입별 추가설정
     */
    private void setRecentSearchByType(RecentSearchDto dto) {

        RecentSearch recentSearch = dto.getRecentSearch();
        switch (recentSearch.getSearchType()) {
            // 캐시 데이터 사이즈 축소
            case AIR -> recentSearch.setData(RecentSearch.convertData(recentSearch.getData()));
        }
    }
}
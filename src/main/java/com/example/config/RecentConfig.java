package com.example.config;

import com.example.domain.recent.Site;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Created by mskwon on 2023/12/15.
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "recent")
public class RecentConfig {

    private Search search;

    @Data
    public static class Search {
        // member_no 만료 시간
        private Duration memberExpire = Duration.ofDays(365);
        // idvisitor 만료 시간
        private Duration idvisitorExpire = Duration.ofMinutes(30);
        // Cache 쓰기 가능 여부 (true: 쓰기 가능, false: 쓰기 불가능)
        private boolean write = true;
        // Cache 삭제 가능 여부 (true: 삭제 가능, false: 삭제 불가능)
        private boolean delete = true;
        // Cache 최대 사이즈
        private Integer maxSize=10;
        // 테스트 사이트
        private Site testSite;
    }

    @PostConstruct
    private void init() {
        log.debug("recent-config: {}", this);
    }
}

package com.ootd.fitme.infrastructure.scraper;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.scraper.exception.ScraperException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmartScraperManager {

    private final JsoupScraper jsoupScraper;
    private final PlaywrightScraper playwrightScraper;

    private static final List<String> BLOCKED_DOMAINS = List.of("coupang");

    private static final List<String> HEAVY_DOMAINS = List.of(
            "musinsa", "naver", "29cm", "zigzag"
    );

    public ScrapedData scrape(String url) {
        String lowerUrl = url.toLowerCase();

        boolean requiresPlaywright = HEAVY_DOMAINS.stream().anyMatch(url::contains);

        if (BLOCKED_DOMAINS.stream().anyMatch(lowerUrl::contains)) {
            log.warn("[SmartScraper] 지원하지 않는 도메인 요청 차단: {}", url);
            throw new ScraperException(ErrorCode.UNSUPPORTED_DOMAIN);
        }

        if (requiresPlaywright) {
            log.info("[SmartScraper] 헤비 도메인 감지됨, Playwright로 직접 요청합니다. URL: {}", url);
            return playwrightScraper.scrape(url);
        }

        try {
            log.info("[SmartScraper] Jsoup 스크래핑 시도 중... URL: {}", url);
            return jsoupScraper.scrape(url);
        } catch (Exception e) {
            log.warn("[SmartScraper] Jsoup 스크래핑 실패 (원인: {}). Playwright로 Fallback 시도합니다.", e.getMessage());
            return playwrightScraper.scrape(url);
        }
    }
}
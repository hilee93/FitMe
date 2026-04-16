package com.ootd.fitme.infrastructure.scraper;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.scraper.exception.ScraperException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScraperManagerUnitTest {

    @InjectMocks
    private ScraperManager scraperManager;

    @Mock
    private JsoupScraper jsoupScraper;

    @Mock
    private PlaywrightScraper playwrightScraper;

    @Test
    @DisplayName("차단된 도메인(coupang) 요청 시 예외를 던지고 스크래퍼를 호출하지 않는다")
    void scrape_blocked_domain() {
        // given
        String targetUrl = "https://www.coupang.com/vp/products/123";

        // when & then
        assertThatThrownBy(() -> scraperManager.scrape(targetUrl))
                .isInstanceOf(ScraperException.class)
                // ErrorCode 객체를 직접 검증하거나 메시지를 검증할 수 있습니다.
                .hasMessageContaining(ErrorCode.UNSUPPORTED_DOMAIN.getMessage());

        // Verify: 어떠한 스크래퍼도 호출되지 않았음을 검증
        verifyNoInteractions(jsoupScraper, playwrightScraper);
    }

    @Test
    @DisplayName("헤비 도메인(musinsa 등) 요청 시 Jsoup을 건너뛰고 Playwright로 바로 스크래핑한다")
    void scrape_heavy_domain() {
        // given
        String targetUrl = "https://www.musinsa.com/products/12345";
        ScrapedData expectedData = new ScrapedData("무신사 옷", "이미지", "본문");

        when(playwrightScraper.scrape(targetUrl)).thenReturn(expectedData);

        // when
        ScrapedData result = scraperManager.scrape(targetUrl);

        // then
        assertThat(result).isEqualTo(expectedData);

        // Verify: Playwright만 1번 호출되고, Jsoup은 호출되지 않았는지 검증
        verify(playwrightScraper, times(1)).scrape(targetUrl);
        verifyNoInteractions(jsoupScraper);
    }

    @Test
    @DisplayName("일반 도메인 요청 시 먼저 Jsoup 스크래핑을 시도하여 성공한다")
    void scrape_normal_domain_success_with_jsoup() throws IOException {
        // given
        String targetUrl = "https://www.some-ordinary-shop.com/item/1";
        ScrapedData expectedData = new ScrapedData("일반 옷", "이미지", "본문");

        // Jsoup이 정상 처리됨을 모킹
        when(jsoupScraper.scrape(targetUrl)).thenReturn(expectedData);

        // when
        ScrapedData result = scraperManager.scrape(targetUrl);

        // then
        assertThat(result).isEqualTo(expectedData);

        // Verify: Jsoup만 1번 호출되고, Playwright는 호출되지 않았는지 검증
        verify(jsoupScraper, times(1)).scrape(targetUrl);
        verifyNoInteractions(playwrightScraper);
    }

    @Test
    @DisplayName("일반 도메인에서 Jsoup 스크래핑 실패 시 Playwright로 Fallback 하여 성공한다")
    void scrape_normal_domain_fallback_to_playwright() throws IOException {
        // given
        String targetUrl = "https://www.some-js-rendered-shop.com/item/2";
        ScrapedData expectedData = new ScrapedData("JS 렌더링 옷", "이미지", "본문");

        // 1. Jsoup은 에러를 던지도록 모킹 (JS 렌더링 사이트라 데이터 부족 등의 이유)
        when(jsoupScraper.scrape(targetUrl)).thenThrow(new RuntimeException("데이터 부족"));

        // 2. Playwright는 성공하도록 모킹
        when(playwrightScraper.scrape(targetUrl)).thenReturn(expectedData);

        // when
        ScrapedData result = scraperManager.scrape(targetUrl);

        // then
        assertThat(result).isEqualTo(expectedData);

        // Verify: Jsoup과 Playwright 모두 각각 1번씩 호출되었는지 검증 (Fallback 동작 확인)
        verify(jsoupScraper, times(1)).scrape(targetUrl);
        verify(playwrightScraper, times(1)).scrape(targetUrl);
    }
}
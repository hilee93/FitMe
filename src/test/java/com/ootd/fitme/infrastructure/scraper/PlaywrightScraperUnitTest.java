package com.ootd.fitme.infrastructure.scraper;

import com.microsoft.playwright.*;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.scraper.exception.ScraperException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaywrightScraperUnitTest {

    @InjectMocks
    private PlaywrightScraper playwrightScraper;

    @Mock private Playwright playwrightMock;
    @Mock private BrowserType browserTypeMock;
    @Mock private Browser browserMock;
    @Mock private BrowserContext browserContextMock;
    @Mock private Page pageMock;

    private static MockedStatic<Playwright> mockedPlaywright;

    @BeforeEach
    void setUp() {
        mockedPlaywright = mockStatic(Playwright.class);
        mockedPlaywright.when(Playwright::create).thenReturn(playwrightMock);

        when(playwrightMock.chromium()).thenReturn(browserTypeMock);
        when(browserTypeMock.launch(any(BrowserType.LaunchOptions.class))).thenReturn(browserMock);

        playwrightScraper.init();
    }

    @AfterEach
    void tearDown() {
        mockedPlaywright.close();
        playwrightScraper.close();
    }

    @Test
    @DisplayName("정상적인 URL이 주어지면 스크래핑에 성공하여 ScrapedData를 반환한다")
    void scrape_success() {
        // given
        String targetUrl = "https://www.musinsa.com/products/12345";
        String expectedTitle = "멋진 트렌치 코트";
        String expectedImage = "https://image.msscdn.net/test.jpg";
        String expectedBody = "본문 상세 정보입니다.";

        // BrowserContext 와 Page 생성 모킹
        when(browserMock.newContext(any(Browser.NewContextOptions.class))).thenReturn(browserContextMock);
        when(browserContextMock.newPage()).thenReturn(pageMock);

        // 페이지 네비게이션 모킹 (void 메서드이므로 doNothing 사용)
        when(pageMock.navigate(eq(targetUrl), any())).thenReturn(null);

        // JS Evaluate 모킹 (스크롤 및 타이틀 추출용)
        when(pageMock.evaluate(anyString())).thenAnswer(invocation -> {
            String script = invocation.getArgument(0);
            if (script.contains("window.scrollBy")) return null; // 스크롤 무시
            if (script.contains("document.querySelectorAll('script")) return expectedTitle; // 타이틀 추출 스크립트일 때
            return null;
        });

        // 이미지 및 요약 설명 추출용 getAttribute 모킹
        when(pageMock.getAttribute("meta[property='og:image']", "content")).thenReturn(expectedImage);
        when(pageMock.getAttribute("meta[property='og:description']", "content")).thenReturn("요약 설명");

        // 본문 텍스트 추출 모킹
        when(pageMock.innerText("body")).thenReturn(expectedBody);

        // Frame 추출 모킹 (NullPointerException 방지)
        when(pageMock.frames()).thenReturn(Collections.emptyList());

        // when
        ScrapedData result = playwrightScraper.scrape(targetUrl);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(expectedTitle);
        assertThat(result.imageUrl()).isEqualTo(expectedImage);
        assertThat(result.coreText()).contains("[요약설명] 요약 설명");
        assertThat(result.coreText()).contains("[본문정보] " + expectedBody);

        // Verify: 특정 메서드들이 정확히 호출되었는지 검증
        verify(pageMock, times(1)).navigate(eq(targetUrl), any());
        verify(pageMock, times(2)).evaluate("window.scrollBy(0, 800)");
    }

    @Test
    @DisplayName("스크래핑 중 에러가 발생하면 ScraperException을 던진다")
    void scrape_fail_throws_scraper_exception() {
        // given
        String targetUrl = "https://invalid-url.com";

        when(browserMock.newContext(any())).thenReturn(browserContextMock);
        when(browserContextMock.newPage()).thenReturn(pageMock);

        // 네비게이션 시 강제로 PlaywrightException 발생
        when(pageMock.navigate(eq(targetUrl), any()))
                .thenThrow(new PlaywrightException("Connection refused"));

        // when & then
        assertThatThrownBy(() -> playwrightScraper.scrape(targetUrl))
                .isInstanceOf(ScraperException.class)
                .hasMessageContaining(ErrorCode.SCRAP_FAILED.getMessage()); // ErrorCode 내부에 정의된 메시지나 코드로 검증 변경 가능
    }
}
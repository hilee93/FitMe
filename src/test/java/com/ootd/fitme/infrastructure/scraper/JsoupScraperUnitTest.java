package com.ootd.fitme.infrastructure.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsoupScraperUnitTest {

    @InjectMocks
    private JsoupScraper jsoupScraper;

    @Test
    @DisplayName("OG 태그가 있는 정상적인 HTML이면 스크래핑에 성공한다")
    void scrape_success_with_og_tags() throws IOException {
        String targetUrl = "https://shop.com/123";
        String longBodyText = "이 옷은 정말 훌륭합니다. ".repeat(20);

        String html = "<html><head>" +
                "<meta property='og:title' content='오버핏 후드티'>" +
                "<meta property='og:image' content='https://img.com/hoodie.jpg'>" +
                "<meta property='og:description' content='편안한 후드티입니다.'>" +
                "</head><body>" + longBodyText + "</body></html>";
        Document realDocument = Jsoup.parse(html);

        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            // RETURNS_DEEP_STUBS를 사용해 체이닝된 메서드 전체를 한 번에 모킹
            Connection connectionMock = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
            mockedJsoup.when(() -> Jsoup.connect(targetUrl)).thenReturn(connectionMock);

            // userAgent -> timeout -> get 으로 이어지는 호출을 정확히 캐치해서 진짜 문서를 리턴
            when(connectionMock.userAgent(anyString()).timeout(anyInt()).get()).thenReturn(realDocument);

            ScrapedData result = jsoupScraper.scrape(targetUrl);

            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("오버핏 후드티");
            assertThat(result.imageUrl()).isEqualTo("https://img.com/hoodie.jpg");
            assertThat(result.coreText()).contains("[상품설명] 편안한 후드티입니다.");
            assertThat(result.coreText()).contains("[본문] 이 옷은 정말 훌륭합니다.");
        }
    }

    @Test
    @DisplayName("OG 태그가 없으면 title과 img 태그 속성으로 Fallback하여 성공한다")
    void scrape_success_fallback_tags() throws IOException {
        String targetUrl = "https://shop.com/456";
        String longBodyText = "상세페이지 본문 내용입니다. ".repeat(20);

        String html = "<html><head>" +
                "<title>스트릿 팬츠 - 쇼핑몰</title>" +
                "</head><body>" +
                "<img class='product-image' src='https://img.com/pants.jpg'>" +
                "<p>" + longBodyText + "</p>" +
                "</body></html>";
        Document realDocument = Jsoup.parse(html);

        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
            mockedJsoup.when(() -> Jsoup.connect(targetUrl)).thenReturn(connectionMock);
            when(connectionMock.userAgent(anyString()).timeout(anyInt()).get()).thenReturn(realDocument);

            ScrapedData result = jsoupScraper.scrape(targetUrl);

            assertThat(result.title()).isEqualTo("스트릿 팬츠");
            assertThat(result.imageUrl()).isEqualTo("https://img.com/pants.jpg");
        }
    }

    @Test
    @DisplayName("타이틀이 없거나 텍스트 길이가 100자 미만이면 예외가 발생한다")
    void scrape_fail_insufficient_data() throws IOException {
        String targetUrl = "https://shop.com/empty";
        String html = "<html><head><title>테스트</title></head><body>짧은 내용</body></html>";
        Document realDocument = Jsoup.parse(html);

        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
            mockedJsoup.when(() -> Jsoup.connect(targetUrl)).thenReturn(connectionMock);
            when(connectionMock.userAgent(anyString()).timeout(anyInt()).get()).thenReturn(realDocument);

            assertThatThrownBy(() -> jsoupScraper.scrape(targetUrl))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("정적 렌더링 데이터가 부족합니다.");
        }
    }

    @Test
    @DisplayName("네트워크 연결 실패 시 IOException을 던진다")
    void scrape_fail_io_exception() {
        String targetUrl = "https://error.com";

        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            Connection connectionMock = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
            mockedJsoup.when(() -> Jsoup.connect(targetUrl)).thenReturn(connectionMock);

            // IOException 강제 발생
            try {
                when(connectionMock.userAgent(anyString()).timeout(anyInt()).get()).thenThrow(new IOException("Timeout"));
            } catch (IOException e) {
                // Mockito checked exception stubbing rule
            }

            assertThatThrownBy(() -> jsoupScraper.scrape(targetUrl))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Timeout");
        }
    }
}
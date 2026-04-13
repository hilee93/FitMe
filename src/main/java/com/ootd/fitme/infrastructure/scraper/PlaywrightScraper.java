package com.ootd.fitme.infrastructure.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.scraper.exception.ScraperException;
import com.sun.management.OperatingSystemMXBean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Component
public class PlaywrightScraper implements Scraper {

    // 스프링 컨테이너 라이프사이클 동안 단 1개만 유지되는 브라우저 인스턴스
    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        log.info("[Playwright] 서버 시작 시 백그라운드 브라우저 1개를 띄웁니다...");
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @PreDestroy
    public void close() {
        log.info("[Playwright] 서버 종료 시 브라우저를 닫습니다.");
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Override
    public ScrapedData scrape(String url) {

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080)
                .setExtraHTTPHeaders(Map.ofEntries(
                        Map.entry("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"),
                        Map.entry("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"),
                        Map.entry("Referer", "https://www.google.com/"),
                        Map.entry("sec-ch-ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\""),
                        Map.entry("sec-ch-ua-mobile", "?0"),
                        Map.entry("sec-ch-ua-platform", "\"macOS\""),
                        Map.entry("Upgrade-Insecure-Requests", "1"),
                        Map.entry("Sec-Fetch-Dest", "document"),
                        Map.entry("Sec-Fetch-Mode", "navigate"),
                        Map.entry("Sec-Fetch-Site", "cross-site"),
                        Map.entry("Sec-Fetch-User", "?1")
                ));

        try (BrowserContext context = browser.newContext(contextOptions);
             Page page = context.newPage()) {

            context.addInitScript("() => {" +
                    "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +
                    "window.navigator.chrome = { runtime: {} };" +
                    "Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3]});" +
                    "Object.defineProperty(navigator, 'languages', {get: () => ['ko-KR', 'ko', 'en-US', 'en']});" +
                    "}");


            page.navigate(url, new Page.NavigateOptions()
                    .setTimeout(15000)
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            page.evaluate("window.scrollBy(0, 800)");
            page.waitForTimeout(500);
            page.evaluate("window.scrollBy(0, 800)");
            page.waitForTimeout(500);

            String imageUrl = extractImageUrl(page);
            String exactProductName = extractTitleByJS(page);
            String coreText = extractAiFriendlyData(page);

            log.info("[Playwright] 스크래핑 성공 - 상품명: {}, 이미지 URL: {}", exactProductName, imageUrl);
            log.debug("[Playwright] 추출된 핵심 텍스트:\n{}", coreText);

            return new ScrapedData(exactProductName, imageUrl, coreText);

        } catch (Exception e) {
            log.error("[Playwright] 스크래핑 차단 또는 실패 - URL: {}", url, e);
            throw new ScraperException(ErrorCode.SCRAP_FAILED);
        }
    }

    private String extractImageUrl(Page page) {
        String url = getAttributeSilently(page, "meta[property='og:image']", "content");
        if (url == null || url.isBlank()) {
            url = getAttributeSilently(page, "img[class*='product'], img[id*='product'], img[class*='thumb']", "src");
        }
        return url != null ? url : "";
    }

    private String extractAiFriendlyData(Page page) {
        StringBuilder sb = new StringBuilder();

        try {
            String ogDesc = getAttributeSilently(page, "meta[property='og:description']", "content");
            if (!ogDesc.isBlank()) {
                sb.append("[상품설명] ").append(ogDesc).append("\n");
            }

            try {
                sb.append("[메인본문] ").append(page.innerText("body")).append("\n");
            } catch (Exception e) {
                log.warn("[Playwright] 메인 바디 텍스트 추출 실패");
            }

            for (Frame frame : page.frames()) {
                if (frame != page.mainFrame()) {
                    try {
                        String frameText = frame.innerText("body");
                        if (frameText != null && !frameText.isBlank()) {
                            sb.append("[추가정보] ").append(frameText).append("\n");
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            String result = sb.toString().replaceAll("\\s+", " ").trim();

            return result.substring(0, Math.min(result.length(), 2000));

        } catch (Exception e) {
            log.error("[Playwright] 텍스트 추출 중 치명적 에러 발생", e);
            return "";
        }
    }

    private String extractTitleByJS(Page page) {
        try {
            return (String) page.evaluate("() => {" +
                    "   try {" +
                    "       let scripts = document.querySelectorAll('script[type=\"application/ld+json\"]');" +
                    "       for (let s of scripts) {" +
                    "           let data = JSON.parse(s.innerText);" +
                    "           let items = Array.isArray(data) ? data : [data];" +
                    "           for (let item of items) {" +
                    "               if (item['@type'] === 'Product' && item.name) return item.name;" +
                    "           }" +
                    "       }" +
                    "   } catch(e) {}" +
                    "   let ogTitle = document.querySelector('meta[property=\"og:title\"]');" +
                    "   let titleText = ogTitle ? ogTitle.content : document.title;" +
                    "   return titleText.split(/[-|]/)[0].trim();" +
                    "}");
        } catch (Exception e) {
            return "";
        }
    }

    private String getAttributeSilently(Page page, String selector, String attribute) {
        try {
            return (String) page.getAttribute(selector, attribute);
        } catch (Exception e) {
            return "";
        }
    }
}
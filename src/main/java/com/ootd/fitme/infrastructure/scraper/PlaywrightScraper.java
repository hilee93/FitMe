package com.ootd.fitme.infrastructure.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.scraper.exception.ScraperException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlaywrightScraper {

    public record ScrapedData(String title, String imageUrl) {}

    public ScrapedData scrape(String url) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080);

            try (BrowserContext context = browser.newContext(contextOptions);
                 Page page = context.newPage()) {

                page.navigate(url, new Page.NavigateOptions()
                        .setTimeout(10000)
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                page.evaluate("window.scrollTo(0, 500)");
                page.waitForTimeout(500);

                // 데이터 추출
                String imageUrl = getAttributeSilently(page, "meta[property='og:image']", "content");
                String exactProductName = extractTitleByJS(page);

                log.info("[Playwright] 파싱 완료 - 상품명: {}, 이미지 URL: {}", exactProductName, imageUrl);

                return new ScrapedData(exactProductName, imageUrl);
            }

        } catch (PlaywrightException e) {
            log.error("[Playwright] 스크래핑 시간 초과 또는 실패 - URL: {}", url, e);
            throw new ScraperException(ErrorCode.SCRAP_FAILED);
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
                    "               if (item['@type'] === 'Product' && item.name) {" +
                    "                   return item.name;" +
                    "               }" +
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
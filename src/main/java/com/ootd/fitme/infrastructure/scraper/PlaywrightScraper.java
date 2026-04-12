package com.ootd.fitme.infrastructure.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.scraper.exception.ScraperException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PlaywrightScraper {

    public record ScrapedData(String title, String imageUrl, String coreText) {}

    public ScrapedData scrape(String url) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080)
                    .setExtraHTTPHeaders(Map.of(
                            "Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
                            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
                    ));

            try (BrowserContext context = browser.newContext(contextOptions);
                 Page page = context.newPage()) {

                context.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

                page.navigate(url, new Page.NavigateOptions()
                        .setTimeout(15000)
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                page.evaluate("window.scrollTo(0, document.body.scrollHeight / 3)");
                page.waitForTimeout(1000);

                String imageUrl = extractImageUrl(page);
                String exactProductName = extractTitleByJS(page);
                String coreText = extractAiFriendlyData(page);

                log.info("[Playwright] 스크래핑 성공 - 상품명: {}, 이미지 URL: {}", exactProductName, imageUrl);
                log.debug("[Playwright] 추출된 핵심 텍스트:\n{}", coreText);

                return new ScrapedData(exactProductName, imageUrl, coreText);
            }

        } catch (PlaywrightException e) {
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
        try {
            return (String) page.evaluate("() => {" +
                    "   let info = '';" +

                    "   /* 1. 카테고리 (성별, 옷 종류 파악용) */" +
                    "   let breadcrumbs = document.querySelectorAll('[class*=\"breadcrumb\"], [class*=\"category\"], [id*=\"location\"]');" +
                    "   if (breadcrumbs.length > 0) {" +
                    "       info += '[카테고리] ' + breadcrumbs[0].innerText.replace(/\\n/g, ' > ') + '\\n';" +
                    "   }" +

                    "   /* 2. 상세 설명 메타데이터 */" +
                    "   let ogDesc = document.querySelector('meta[property=\"og:description\"]');" +
                    "   if (ogDesc && ogDesc.content) {" +
                    "       info += '[상품설명] ' + ogDesc.content + '\\n';" +
                    "   }" +

                    "   /* 3. 옵션 드롭다운 (색상, 사이즈 등 파악용) */" +
                    "   let selects = document.querySelectorAll('select');" +
                    "   selects.forEach(s => {" +
                    "       let options = Array.from(s.options).map(o => o.text).filter(t => !t.includes('선택')).join(', ');" +
                    "       if(options.length > 0) info += '[선택옵션] ' + options + '\\n';" +
                    "   });" +

                    "   /* 4. 스펙 테이블 (소재, 핏, 재질 파악용) */" +
                    "   let specs = document.querySelectorAll('table, dl, [class*=\"spec\"], [class*=\"info\"], [class*=\"detail\"]');" +
                    "   specs.forEach(s => {" +
                    "       let text = s.innerText.replace(/\\s+/g, ' ').trim();" +
                    "       if(text.length > 10 && text.length < 300) {" +
                    "           info += '[상세스펙] ' + text + '\\n';" +
                    "       }" +
                    "   });" +

                    "   // 토큰 비용 절약 및 환각 방지를 위해 최대 1500자로 자르기" +
                    "   return info.substring(0, 1500);" +
                    "}");
        } catch (Exception e) {
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
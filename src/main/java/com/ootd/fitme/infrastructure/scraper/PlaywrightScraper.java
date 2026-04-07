package com.ootd.fitme.infrastructure.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlaywrightScraper {

    public record ScrapedData(String title, String imageUrl) {}

    public ScrapedData scrape(String url) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
             Page page = browser.newPage()) {

            // DOM 로딩 완료 시점에 즉시 중단 (초고속)
            page.navigate(url, new Page.NavigateOptions()
                    .setTimeout(15000)
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            // 1. 이미지 추출
            String imageUrl = getAttributeSilently(page, "meta[property='og:image']", "content");

            // 2. 브라우저 내부에서 JS를 실행하여 진짜 '상품명'만 정교하게 추출!
            String exactProductName = (String) page.evaluate("() => {" +
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
                    "   " +
                    "   return titleText.split(/[-|]/)[0].trim();" +
                    "}");

            return new ScrapedData(exactProductName, imageUrl);

        } catch (Exception e) {
            log.error("[Playwright] 스크래핑 실패 - URL: {}", url, e);
            throw new FitmeException(ErrorCode.SCRAP_FAILED);
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
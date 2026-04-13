package com.ootd.fitme.infrastructure.scraper;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JsoupScraper {

    public ScrapedData scrape(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get();

        String imageUrl = doc.select("meta[property=og:image]").attr("content");
        if (imageUrl.isBlank()) {
            imageUrl = doc.select("img[class*=product], img[id*=product]").attr("src");
        }

        String title = doc.select("meta[property=og:title]").attr("content");
        if (title.isBlank()) {
            title = doc.title().split("[-|]")[0].trim();
        }
        String ogDesc = doc.select("meta[property=og:description]").attr("content");
        String bodyText = doc.body().text();

        String coreText = "[상품설명] " + ogDesc + "\n[본문] " +
                bodyText.substring(0, Math.min(bodyText.length(), 1500));

        if (title.isBlank() || coreText.length() < 100) {
            throw new RuntimeException("정적 렌더링 데이터가 부족합니다.");
        }

        log.info("[Jsoup] 스크래핑 성공 - 상품명: {}", title);
        return new ScrapedData(title, imageUrl, coreText);
    }
}
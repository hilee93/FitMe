package com.ootd.fitme.infrastructure.scraper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Slf4j
@Component
public class ImageDownloadUtil {

    private final RestTemplate restTemplate = new RestTemplate();

    // 🌟 원본 쇼핑몰 URL(originalUrl)을 추가로 받습니다.
    public String downloadImageAsBase64(String imageUrl, String originalUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        if (imageUrl.startsWith("//")) {
            imageUrl = "https:" + imageUrl;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "image/*");

            if (originalUrl != null && !originalUrl.isBlank()) {
                headers.set("Referer", originalUrl);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(imageUrl, HttpMethod.GET, entity, byte[].class);
            byte[] imageBytes = response.getBody();

            if (imageBytes == null || imageBytes.length == 0) {
                return null;
            }

            String base64String = Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/jpeg;base64," + base64String;

        } catch (Exception e) {
            log.warn("[ImageDownloadUtil] 외부 이미지 다운로드 실패 (CDN 차단 가능성) - 이미지 URL: {}, 원본 URL: {}", imageUrl, originalUrl);
            return null;
        }
    }
}
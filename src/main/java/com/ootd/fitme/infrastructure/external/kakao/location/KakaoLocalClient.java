package com.ootd.fitme.infrastructure.external.kakao.location;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoLocalClient {
    private final RestClient restClient;
    private final KakaoLocalProperties properties;

    public RegionInfo resolveRegion(double longitude, double latitude) {
        URI uri = UriComponentsBuilder.fromUriString(properties.local().baseUrl()) // base url 시작
                .path(properties.local().coord2regionPath()) // api 경로
                .queryParam("x", longitude) // 경도
                .queryParam("y", latitude) // 위도
                .queryParam("input_coord", "WGS84") // 좌표계 지정
                .build(true) // uri 빌드
                .toUri(); // uri 객체 변환

        KakaoCoord2RegionResponse response = restClient.get() // 클라이언트 get 요청
                .uri(uri) // 위에서 만든 uri 사용
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.restApiKey()) // 인증 헤더
                .retrieve() // 응답 수신
                .body(KakaoCoord2RegionResponse.class); // json -> dto 역직렬화

        List<KakaoCoord2RegionResponse.Document> docs = response == null || response.documents() == null
                ? List.of()
                : response.documents();

        KakaoCoord2RegionResponse.Document picked = docs.stream()
                .filter(d -> "H".equalsIgnoreCase(d.regionType())) // 행정동 우선
                .findFirst()
                .orElseGet(() -> docs.stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Kakao region not found")));

        return new RegionInfo(
                picked.code(),
                picked.addressName(),
                picked.region1depthName(),
                picked.region2depthName(),
                picked.region3depthName(),
                picked.region4depthName(),
                picked.x(),
                picked.y()
        );
    }

    public record RegionInfo(
            String regionCode,
            String addressName,
            String region1depthName,
            String region2depthName,
            String region3depthName,
            String region4depthName,
            Double longitude,
            Double latitude
    ) {}
}

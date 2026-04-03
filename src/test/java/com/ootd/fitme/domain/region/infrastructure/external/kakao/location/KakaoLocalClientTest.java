package com.ootd.fitme.domain.region.infrastructure.external.kakao.location;

import com.ootd.fitme.infrastructure.external.kakao.location.KakaoLocalClient;
import com.ootd.fitme.infrastructure.external.kakao.location.KakaoLocalProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class KakaoLocalClientTest {
    @Test
    @DisplayName("행정동(H) 문서를 우선 선택한다")
    void resolveRegion_pickHFirst() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        KakaoLocalProperties properties = new KakaoLocalProperties(
                "test-key",
                new KakaoLocalProperties.Local("https://dapi.kakao.com", "/v2/local/geo/coord2regioncode.json"));

        KakaoLocalClient client = new KakaoLocalClient(builder.build(), properties);

        String body = """
                {
                  "meta": { "total_count": 2 },
                  "documents": [
                    {
                      "region_type": "B",
                      "address_name": "서울 종로구",
                      "region_1depth_name": "서울",
                      "region_2depth_name": "종로구",
                      "region_3depth_name": "",
                      "region_4depth_name": "",
                      "code": "1111010100",
                      "x": 126.9780,
                      "y": 37.5665
                    },
                    {
                      "region_type": "H",
                      "address_name": "서울 종로구 청운효자동",
                      "region_1depth_name": "서울",
                      "region_2depth_name": "종로구",
                      "region_3depth_name": "청운효자동",
                      "region_4depth_name": "",
                      "code": "1111061500",
                      "x": 126.9707,
                      "y": 37.5841
                    }
                  ]
                }
                """;

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(containsString("https://dapi.kakao.com/v2/local/geo/coord2regioncode.json")))
                .andExpect(requestTo(containsString("x=126.9707")))
                .andExpect(requestTo(containsString("y=37.5841")))
                .andExpect(header("Authorization", "KakaoAK test-key"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        KakaoLocalClient.RegionInfo result = client.resolveRegion(126.9707, 37.5841);

        assertThat(result.regionCode()).isEqualTo("1111061500");
        assertThat(result.region3depthName()).isEqualTo("청운효자동");
        server.verify();
    }

    @Test
    @DisplayName("H가 없으면 첫 번째 문서를 fallback으로 선택한다")
    void resolveRegion_fallbackFirstWhenNoH() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        KakaoLocalProperties properties = new KakaoLocalProperties(
                "test-key",
                new KakaoLocalProperties.Local("https://dapi.kakao.com", "/v2/local/geo/coord2regioncode.json")
        );

        KakaoLocalClient client = new KakaoLocalClient(builder.build(), properties);

        String body = """
                {
                  "meta": { "total_count": 1 },
                  "documents": [
                    {
                      "region_type": "B",
                      "address_name": "서울 종로구",
                      "region_1depth_name": "서울",
                      "region_2depth_name": "종로구",
                      "region_3depth_name": "",
                      "region_4depth_name": "",
                      "code": "1111010100",
                      "x": 126.9780,
                      "y": 37.5665
                    }
                  ]
                }
                """;

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(containsString("https://dapi.kakao.com/v2/local/geo/coord2regioncode.json")))
                .andExpect(requestTo(containsString("x=126.978")))
                .andExpect(requestTo(containsString("y=37.5665")))
                .andExpect(header("Authorization", "KakaoAK test-key"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        KakaoLocalClient.RegionInfo result = client.resolveRegion(126.9780, 37.5665);

        assertThat(result.regionCode()).isEqualTo("1111010100");
        server.verify();
    }

    @Test
    @DisplayName("documents가 비어 있으면 IllegalStateException")
    void resolveRegion_throwWhenNoDocuments() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        KakaoLocalProperties properties = new KakaoLocalProperties(
                "test-key",
                new KakaoLocalProperties.Local("https://dapi.kakao.com", "/v2/local/geo/coord2regioncode.json")
        );

        KakaoLocalClient client = new KakaoLocalClient(builder.build(), properties);

        String body = """
                {
                  "meta": { "total_count": 0 },
                  "documents": []
                }
                """;

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(containsString("https://dapi.kakao.com/v2/local/geo/coord2regioncode.json")))
                .andExpect(requestTo(containsString("x=126.978")))
                .andExpect(requestTo(containsString("y=37.5665")))
                .andExpect(header("Authorization", "KakaoAK test-key"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.resolveRegion(126.9780, 37.5665))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kakao region not found");

        server.verify();
    }
}

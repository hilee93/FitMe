package com.ootd.fitme.domain.catalog.service;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.catalog.entity.CatalogClothes;
import com.ootd.fitme.domain.catalog.repository.CatalogClothesRepository;
import com.ootd.fitme.domain.clothes.dto.AiClothesResult;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.infrastructure.ai.AiDataExtractor;
import com.ootd.fitme.infrastructure.scraper.ScrapedData;
import com.ootd.fitme.infrastructure.scraper.ScraperManager;
import com.ootd.fitme.infrastructure.scraper.UrlUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class CatalogClothesServiceTest {

    @Autowired
    private CatalogClothesService catalogClothesService;

    @Autowired
    private CatalogClothesRepository catalogRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    // 외부 API 통신을 담당하는 클래스들만 Mock 객체로 대체
    @MockitoBean
    private ScraperManager scraperManager;

    @MockitoBean
    private AiDataExtractor aiExtractor;

    @BeforeEach
    void setup() {
         Attribute testAttribute = Attribute.create("성별");
         SelectableValue testValue = SelectableValue.create("MALE", 0,testAttribute);
         testAttribute.getSelectableValues().add(testValue);
         attributeRepository.save(testAttribute);
    }

    @Test
    @DisplayName("[성공] 새로운 URL 요청 시 스크래핑+AI분석을 거쳐 DB에 저장하고 DTO를 반환한다")
    void extractInfoFromLink_FullSuccess() {
        // given
        String testUrl = "https://test.com/product/123";

        ScrapedData scrapedData = new ScrapedData("멋진 슬랙스", "상품 상세 정보", "https://img.com/1.jpg");
        when(scraperManager.scrape(anyString())).thenReturn(scrapedData);

        // setup()에 저장해둔 속성("성별"-"MALE")과 정확히 일치하는 데이터를 AI가 반환했다고 모킹
        AiClothesResult.AiAttribute aiAttr = new AiClothesResult.AiAttribute("성별", "MALE");
        AiClothesResult aiResult = new AiClothesResult("정제된 슬랙스", ClothesType.BOTTOM, List.of(aiAttr));
        when(aiExtractor.extractData(anyString(), anyString(), eq(AiClothesResult.class))).thenReturn(aiResult);

        // when
        ClothesDto result = catalogClothesService.extractInfoFromLink(testUrl);

        // then
        assertThat(result.name()).isEqualTo("정제된 슬랙스");
        assertThat(result.type()).isEqualTo(ClothesType.BOTTOM);
        assertThat(result.attributes()).isNotEmpty(); // 매핑된 속성이 DTO에 잘 담겼는지 확인

        // DB 저장 여부 확인
        String normalizedUrl = UrlUtil.normalize(testUrl);
        Optional<CatalogClothes> saved = catalogRepository.findByOriginalUrl(normalizedUrl);
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("정제된 슬랙스");
    }

    @Test
    @DisplayName("[캐시] 이미 DB에 존재하는 정규화된 URL이면 외부 호출 없이 DB 데이터를 즉시 반환한다")
    void extractInfoFromLink_CacheHit() {
        // given
        String testUrl = "https://test.com/product/already-exists";
        String normalizedUrl = UrlUtil.normalize(testUrl);

        // DB에 미리 상품을 하나 저장해 둡니다.
        CatalogClothes existing = CatalogClothes.create(
                normalizedUrl, "기존캐시상품", "img.jpg", ClothesType.TOP, Map.of("성별", "MALE")
        );
        catalogRepository.save(existing);

        // when
        ClothesDto result = catalogClothesService.extractInfoFromLink(testUrl);

        // then
        assertThat(result.name()).isEqualTo("기존캐시상품");

        // 캐시 Hit가 발생했으므로 스크래퍼나 AI 분석이 단 한 번도 호출되지 않아야 합니다.
        verify(scraperManager, never()).scrape(anyString());
        verify(aiExtractor, never()).extractData(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("[예외/Fallback] AI 분석이 실패해도 에러를 발생시키지 않고 스크래핑 기반 기본 데이터로 응답한다")
    void extractInfoFromLink_AiExceptionFallback() {
        // given
        String testUrl = "https://test.com/product/error";

        ScrapedData scrapedData = new ScrapedData("원본 상품명", "상세 설명", "https://img.com/err.jpg");
        when(scraperManager.scrape(anyString())).thenReturn(scrapedData);

        // AI 통신 중 예외 발생 상황 모킹
        when(aiExtractor.extractData(anyString(), anyString(), eq(AiClothesResult.class)))
                .thenThrow(new RuntimeException("AI API Timeout"));

        // when
        ClothesDto result = catalogClothesService.extractInfoFromLink(testUrl);

        // then
        assertThat(result.name()).isEqualTo("원본 상품명"); // 예외가 터졌으므로 스크래핑된 원본 이름 사용
        assertThat(result.type()).isEqualTo(ClothesType.ETC);
        assertThat(result.attributes()).isEmpty(); // 매핑된 속성이 없어야 함
    }

    @Test
    @DisplayName("[정규화] 파라미터 순서가 다른 동일 URL 요청 시 캐시가 작동하여 스크래핑은 1번만 실행된다")
    void extractInfoFromLink_UrlNormalization() {
        // given
        String url1 = "https://test.com/item?a=1&b=2";
        String url2 = "https://test.com/item?b=2&a=1"; // 정규화 시 url1과 동일해짐

        ScrapedData scrapedData = new ScrapedData("정규화 테스트", "설명", "img.jpg");
        when(scraperManager.scrape(anyString())).thenReturn(scrapedData);

        // 이전 테스트의 실패 원인이었던 부분! 유효한 속성을 담아서 DB에 정상 저장되도록 만듭니다.
        AiClothesResult.AiAttribute validAttr = new AiClothesResult.AiAttribute("성별", "MALE");
        AiClothesResult aiResult = new AiClothesResult("정제된 이름", ClothesType.TOP, List.of(validAttr));
        when(aiExtractor.extractData(anyString(), anyString(), any())).thenReturn(aiResult);

        // when
        catalogClothesService.extractInfoFromLink(url1); // 첫 번째 요청: DB에 없으므로 스크래핑 -> 유효 속성이 있으므로 DB 저장
        catalogClothesService.extractInfoFromLink(url2); // 두 번째 요청: DB에 있으므로 캐시 Hit

        // then
        // 스크래핑이 정확히 한 번만 일어났는지 검증합니다.
        verify(scraperManager, times(1)).scrape(anyString());
        assertThat(catalogRepository.count()).isEqualTo(1); // 마스터 DB에도 딱 한 건만 저장됨
    }
}
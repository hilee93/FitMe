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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogClothesServiceUnitTest {

    @Mock
    private CatalogClothesRepository catalogRepository;
    @Mock
    private ScraperManager scraperManager;
    @Mock
    private AiDataExtractor aiExtractor;
    @Mock
    private AttributeRepository attributeRepository;

    @InjectMocks
    private CatalogClothesService catalogClothesService;

    private String testUrl = "https://test.com/product/1";
    private Attribute mockAttribute;

    @BeforeEach
    void setUp() {
        mockAttribute = mock(Attribute.class);
        lenient().when(mockAttribute.getId()).thenReturn(UUID.randomUUID());
        lenient().when(mockAttribute.getName()).thenReturn("성별");

        SelectableValue mockValue = mock(SelectableValue.class);
        lenient().when(mockValue.getType()).thenReturn("MALE");
        lenient().when(mockAttribute.getSelectableValues()).thenReturn(List.of(mockValue));

        lenient().when(attributeRepository.findAllWithSelectableValues()).thenReturn(List.of(mockAttribute));
    }

    @Test
    @DisplayName("DB에 이미 존재하는 URL이면 스크래핑/AI 호출 없이 DTO를 반환한다 (Cache Hit)")
    void extractInfoFromLink_DbCacheHit() {
        // given
        CatalogClothes existingCatalog = CatalogClothes.create(testUrl, "기존 의류", "image.jpg", ClothesType.TOP, Map.of("성별", "MALE"));
        when(catalogRepository.findByOriginalUrl(anyString())).thenReturn(Optional.of(existingCatalog));

        // when
        ClothesDto result = catalogClothesService.extractInfoFromLink(testUrl);

        // then
        assertThat(result.name()).isEqualTo("기존 의류");
        verify(scraperManager, never()).scrape(anyString());
        verify(aiExtractor, never()).extractData(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("새로운 URL이면 스크래핑과 AI 분석을 통해 유효한 데이터를 추출하고 DB에 저장한다 (Cache Miss)")
    void extractInfoFromLink_ScrapeAndAiSuccess() {
        // given
        when(catalogRepository.findByOriginalUrl(anyString())).thenReturn(Optional.empty());

        ScrapedData scrapedData = new ScrapedData("멋진 티셔츠", "상세설명", "image.jpg");
        when(scraperManager.scrape(anyString())).thenReturn(scrapedData);

        AiClothesResult.AiAttribute validAttr = new AiClothesResult.AiAttribute("성별", "MALE");
        AiClothesResult aiResult = new AiClothesResult("AI 티셔츠", ClothesType.TOP, List.of(validAttr));
        when(aiExtractor.extractData(anyString(), anyString(), eq(AiClothesResult.class))).thenReturn(aiResult);

        // when
        ClothesDto result = catalogClothesService.extractInfoFromLink(testUrl);

        // then
        assertThat(result.name()).isEqualTo("AI 티셔츠");
        assertThat(result.type()).isEqualTo(ClothesType.TOP);
        assertThat(result.attributes()).hasSize(1);
        verify(catalogRepository, times(1)).save(any(CatalogClothes.class));
    }

    @Test
    @DisplayName("AI 분석이 실패해도 에러를 던지지 않고 스크래핑 데이터로 Fallback 처리되며, 속성이 없어 DB에는 저장하지 않는다")
    void extractInfoFromLink_AiFailure_Fallback() {
        // given
        when(catalogRepository.findByOriginalUrl(anyString())).thenReturn(Optional.empty());
        ScrapedData scrapedData = new ScrapedData("스크래핑 원본 제목", "상세설명", "image.jpg");
        when(scraperManager.scrape(anyString())).thenReturn(scrapedData);

        // AI 추출 시 예외 발생
        when(aiExtractor.extractData(anyString(), anyString(), eq(AiClothesResult.class)))
                .thenThrow(new RuntimeException("AI Server Error"));

        // when
        ClothesDto result = catalogClothesService.extractInfoFromLink(testUrl);

        // then
        // Fallback되어 스크래핑 원본 제목과 기본값 반환
        assertThat(result.name()).isEqualTo("스크래핑 원본 제목");
        assertThat(result.type()).isEqualTo(ClothesType.ETC);
        assertThat(result.attributes()).isEmpty();

        // 속성 데이터가 없으므로 DB에 저장하지 않아야 함
        verify(catalogRepository, never()).save(any(CatalogClothes.class));
    }
}
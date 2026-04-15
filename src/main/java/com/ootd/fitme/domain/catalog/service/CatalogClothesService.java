package com.ootd.fitme.domain.catalog.service;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.catalog.entity.CatalogClothes;
import com.ootd.fitme.domain.catalog.repository.CatalogClothesRepository;
import com.ootd.fitme.domain.clothes.dto.AiClothesResult;
import com.ootd.fitme.domain.clothes.dto.ClothesAttributeWithDefDto;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.infrastructure.ai.AiDataExtractor;
import com.ootd.fitme.infrastructure.scraper.ScrapedData;
import com.ootd.fitme.infrastructure.scraper.ScraperManager;
import com.ootd.fitme.infrastructure.scraper.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogClothesService {

    private final CatalogClothesRepository catalogRepository;
    private final ScraperManager scraperManager;
    private final AiDataExtractor aiExtractor;
    private final AttributeRepository attributeRepository;

    @Cacheable(value = "clothes_extraction",
            key = "T(com.ootd.fitme.infrastructure.scraper.UrlUtil).normalize(#link)",
            unless = "#result == null || #result.attributes().isEmpty()")
    public ClothesDto extractInfoFromLink(String link) {
        String normalizedUrl = UrlUtil.normalize(link);
        log.info("[CatalogService] 의상 정보 추출 요청: {}", normalizedUrl);

        Optional<CatalogClothes> existingCatalog = catalogRepository.findByOriginalUrl(normalizedUrl);
        if (existingCatalog.isPresent()) {
            log.info("[CatalogService] DB 캐시 Hit! AI 서버를 호출하지 않습니다.");
            return convertToDto(existingCatalog.get());
        }

        log.info("[CatalogService] DB 캐시 Miss. 스크래핑 및 AI 분석을 시작합니다.");
        ScrapedData scrapedData = scraperManager.scrape(normalizedUrl);

        List<Attribute> allAttributes = attributeRepository.findAll();
        String attributePromptGuide = buildAttributePromptGuide(allAttributes);

        String systemInstruction = "You are an expert fashion data analyst.\n" +
                "Read the provided product information and extract the clothing details.\n" +
                "1. name: Summarize the core product name in about 20 characters in Korean.\n" +
                "2. type: Choose EXACTLY ONE from this list: [ALL, TOP, BOTTOM, DRESS, OUTER, UNDERWEAR, SHOES, SOCKS, HAT, BAG, ACCESSORY, SCARF, ETC]\n" +
                "3. attributes: Map the product details to the [Allowed Attributes & Options] provided below.\n" +
                "   - Actively INFER attributes like gender, fit, material, and season from the context.\n" +
                "   - STRICT RULE 1: Select EXACTLY ONE value per attribute definition. DO NOT output duplicate definitionNames.\n" +
                "   - STRICT RULE 2: You MUST ONLY use the EXACT 'definitionName' listed in the [Allowed Attributes & Options]. DO NOT invent new attributes (e.g., do not make up '색상', '상의 카테고리').\n" +
                "   - STRICT RULE 3: You MUST ONLY select a 'value' from the provided '(선택가능 옵션)'. DO NOT invent or modify options.\n" +
                "   - Omit unknown attributes.\n\n" +
                "[Allowed Attributes & Options]\n" + attributePromptGuide;

        String rawData = String.format("[원본 상품명]: %s\n[상세 정보]:\n%s", scrapedData.title(), scrapedData.coreText());
        AiClothesResult aiResult = null;

        try {
            aiResult = aiExtractor.extractData(rawData, systemInstruction, AiClothesResult.class);
        } catch (Exception e) {
            log.warn("[CatalogService] AI 분석 실패. 기본 데이터로 Fallback 진행. (원인: {})", e.getMessage());
        }

        String finalImageUrl = (scrapedData.imageUrl() != null && !scrapedData.imageUrl().isBlank()) ? scrapedData.imageUrl() : "";
        ClothesType finalType = (aiResult != null && aiResult.type() != null) ? aiResult.type() : ClothesType.ETC;
        String finalName = (aiResult != null && aiResult.name() != null && !aiResult.name().isBlank())
                ? HtmlUtils.htmlEscape(aiResult.name()) : HtmlUtils.htmlEscape(scrapedData.title());

        if (finalName.length() > 100) {
            finalName = finalName.substring(0, 100);
        }

        Map<String, Object> attributesMap = new HashMap<>();

        if (aiResult != null && aiResult.attributes() != null) {
            for (AiClothesResult.AiAttribute aiAttr : aiResult.attributes()) {
                String aiDefName = aiAttr.definitionName();
                String aiValue = aiAttr.value();

                Optional<Attribute> matchedAttr = allAttributes.stream()
                        .filter(dbAttr -> dbAttr.getName().equalsIgnoreCase(aiDefName))
                        .findFirst();

                if (matchedAttr.isPresent()) {
                    boolean isValidValue = matchedAttr.get().getSelectableValues().stream()
                            .anyMatch(sv -> sv.getType().equalsIgnoreCase(aiValue));

                    if (isValidValue && !attributesMap.containsKey(aiDefName)) {
                        attributesMap.put(aiDefName, aiValue);
                    } else if (!isValidValue) {
                        log.warn("[CatalogService] 필터링 됨 (잘못된 옵션값): 속성 [{}]에 허용되지 않은 값 [{}]", aiDefName, aiValue);
                    }
                } else {
                    log.warn("[CatalogService] 필터링 됨 (없는 속성명): 허용되지 않은 임의의 속성 [{}]", aiDefName);
                }
            }
        }

        if (!attributesMap.isEmpty()) {
            CatalogClothes newCatalog = CatalogClothes.create(normalizedUrl, finalName, finalImageUrl, finalType, attributesMap);
            catalogRepository.save(newCatalog);
            log.info("[CatalogService] [성공] 새로운 카탈로그 DB 저장 완료: {}", finalName);
            return convertToDto(newCatalog);
        } else {
            log.warn("[CatalogService] [실패/거절] AI 추출 데이터가 유효하지 않아 마스터 DB에 저장하지 않습니다.");
            return new ClothesDto(null, null, finalName, finalImageUrl, finalType, new ArrayList<>());
        }
    }

    // --- 내부 헬퍼 메서드 ---

    private String buildAttributePromptGuide(List<Attribute> attributes) {
        StringBuilder guide = new StringBuilder();
        for (Attribute attr : attributes) {
            String options = attr.getSelectableValues().stream()
                    .map(sv -> sv.getType())
                    .collect(Collectors.joining(", "));
            guide.append("- ").append(attr.getName()).append(" (선택가능 옵션: ").append(options).append(")\n");
        }
        return guide.toString();
    }

    private ClothesDto convertToDto(CatalogClothes catalog) {
        List<Attribute> allAttributes = attributeRepository.findAll();
        List<ClothesAttributeWithDefDto> mappedAttributes = new ArrayList<>();

        for (Map.Entry<String, Object> entry : catalog.getAttributes().entrySet()) {
            String defName = entry.getKey();
            String value = String.valueOf(entry.getValue());

            allAttributes.stream()
                    .filter(dbAttr -> dbAttr.getName().equalsIgnoreCase(defName))
                    .findFirst()
                    .ifPresent(dbAttr -> {
                        List<String> selectableOptions = dbAttr.getSelectableValues().stream()
                                .map(sv -> sv.getType())
                                .toList();
                        mappedAttributes.add(new ClothesAttributeWithDefDto(
                                dbAttr.getId(), dbAttr.getName(), selectableOptions, value
                        ));
                    });
        }

        return new ClothesDto(null, null, catalog.getName(), catalog.getImageUrl(), catalog.getType(), mappedAttributes);
    }
}
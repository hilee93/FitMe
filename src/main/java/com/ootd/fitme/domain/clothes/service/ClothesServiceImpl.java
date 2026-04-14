package com.ootd.fitme.domain.clothes.service;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.dto.*;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.exception.ClothesException;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.service.MediaFileService;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.domain.selectablevalue.repository.SelectableValueRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.ai.AiDataExtractor;
import com.ootd.fitme.infrastructure.scraper.JsoupScraper;
import com.ootd.fitme.infrastructure.scraper.PlaywrightScraper;
import com.ootd.fitme.infrastructure.scraper.ScrapedData;
import com.ootd.fitme.infrastructure.scraper.SmartScraperManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesServiceImpl implements ClothesService {

    private final ClothesRepository clothesRepository;
    private final UserRepository userRepository;
    private final AttributeRepository attributeRepository;
    private final SelectableValueRepository selectableValueRepository;

    private final MediaFileService mediaFileService;
    private final PlaywrightScraper playwrightScraper;
    private final JsoupScraper jsoupScraper;
    private final AiDataExtractor aiExtractor;

    private final SmartScraperManager scraperManager;

    @Override
    @Transactional
    public ClothesDto createClothes(ClothesCreateRequest request, MultipartFile image, UUID loginUserId) {
        log.info("[ClothesService] 옷 생성 요청 시작 - loginUserId: {}, requestOwnerId: {}, type: {}", loginUserId, request.ownerId(), request.type());

        if (!loginUserId.equals(request.ownerId())) {
            log.warn("[ClothesService] 권한 없음: 타인 명의로 옷 생성 시도 - loginUserId: {}, requestOwnerId: {}", loginUserId, request.ownerId());
            throw new ClothesException(ErrorCode.AUTH_FORBIDDEN);
        }

        User user = userRepository.findById(request.ownerId())
                .orElseThrow(() -> {
                    log.warn("[ClothesService] 옷 생성 실패: 존재하지 않는 사용자 - userId: {}", request.ownerId());
                    return new ClothesException(ErrorCode.CLOTHES_OWNER_NOT_FOUND);
                });

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = mediaFileService.uploadAndRegister(image, MediaPurpose.CLOTHES, user);
            log.info("[ClothesService] 미디어 서비스 통해 이미지 등록 완료 - URL: {}", imageUrl);
        }

        String safeName = HtmlUtils.htmlEscape(request.name());
        Clothes clothes = Clothes.createWithImage(safeName, request.type(), user, imageUrl);

        List<ClothesAttribute> attributes = buildClothesAttributes(clothes, request.attributes());
        clothes.replaceAttributes(attributes);

        Clothes savedClothes = clothesRepository.save(clothes);
        log.info("[ClothesService] 옷 생성 완료 - clothesId: {}, name: {}", savedClothes.getId(), savedClothes.getName());

        List<ClothesAttributeWithDefDto> attributeDtos = buildAttributeDtos(attributes);

        return new ClothesDto(
                savedClothes.getId(),
                user.getId(),
                savedClothes.getName(),
                savedClothes.getImageUrl(),
                savedClothes.getClothesType(),
                attributeDtos
        );
    }

    @Override
    @Transactional
    public ClothesDto updateClothes(UUID clothesId, UUID loginUserId, ClothesUpdateRequest request, MultipartFile newImage) {
        log.info("[ClothesService] 옷 수정 요청 시작 - clothesId: {}, loginUserId: {}", clothesId, loginUserId);

        Clothes clothes = clothesRepository.findByIdWithDetails(clothesId)
                .orElseThrow(() -> {
                    log.warn("[ClothesService] 옷 수정 실패: 존재하지 않는 옷 - clothesId: {}", clothesId);
                    return new ClothesException(ErrorCode.CLOTHES_NOT_FOUND);
                });
        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new ClothesException(ErrorCode.CLOTHES_OWNER_NOT_FOUND));

        if (!clothes.getUser().getId().equals(loginUserId)) {
            log.warn("[ClothesService] 옷 수정 권한 없음 - clothesId: {}, loginUserId: {}", clothesId, loginUserId);
            throw new ClothesException(ErrorCode.AUTH_FORBIDDEN);
        }

        String oldImageUrl = clothes.getImageUrl();
        String newImageUrl = oldImageUrl;

        if (newImage != null && !newImage.isEmpty()) {
            if (clothes.getImageUrl() != null) {
                mediaFileService.deleteMedia(clothes.getImageUrl(), loginUserId);
                log.info("[ClothesService] 기존 이미지 삭제 요청 완료 - oldUrl: {}", clothes.getImageUrl());
            }

            newImageUrl = mediaFileService.uploadAndRegister(newImage, MediaPurpose.CLOTHES, user);
        }

        String safeName = request.name() != null ? HtmlUtils.htmlEscape(request.name()) : clothes.getName();
        clothes.updateClothesInfo(safeName, request.type(), newImageUrl);

        List<ClothesAttribute> incomingAttributes = buildClothesAttributes(clothes, request.attributes());
        clothes.updateAttributes(incomingAttributes);

        log.info("[ClothesService] 옷 수정 완료 - clothesId: {}", clothesId);

        List<ClothesAttributeWithDefDto> updatedAttributeDtos = buildAttributeDtos(incomingAttributes);

        return new ClothesDto(
                clothes.getId(),
                clothes.getUser().getId(),
                clothes.getName(),
                clothes.getImageUrl(),
                clothes.getClothesType(),
                updatedAttributeDtos
        );
    }

    @Override
    @Transactional
    public void deleteClothes(UUID clothesId, UUID loginUserId) {
        log.info("[ClothesService] 옷 삭제 요청 시작 - clothesId: {}, loginUserId: {}", clothesId, loginUserId);

        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> {
                    log.warn("[ClothesService] 옷 삭제 실패: 존재하지 않는 옷 - clothesId: {}", clothesId);
                    return new ClothesException(ErrorCode.CLOTHES_NOT_FOUND);
                });

        if (!clothes.getUser().getId().equals(loginUserId)) {
            log.warn("[ClothesService] 권한 없음: 타인의 옷 삭제 시도 - clothesId: {}, loginUserId: {}, ownerId: {}", clothesId, loginUserId, clothes.getUser().getId());
            throw new ClothesException(ErrorCode.AUTH_FORBIDDEN);
        }

        String imageUrlToDelete = clothes.getImageUrl();

        if (imageUrlToDelete != null) {
            mediaFileService.deleteMedia(imageUrlToDelete, loginUserId);
            log.info("[ClothesService] 연관 미디어 삭제 이벤트 발행 완료 - imageUrl: {}", imageUrlToDelete);
        }

        clothesRepository.deleteByIdInBulk(clothesId);
        log.info("[ClothesService] 옷 DB 삭제 완료 - clothesId: {}", clothesId);
    }

    @Override
    public ClothesDtoCursorResponse getClothesList(ClothesDtoCursorRequest request, UUID loginUserId) {
        log.info("[ClothesService] 옷 목록 커서 조회 요청 - loginUserId: {}, requestOwnerId: {}, limit: {}", loginUserId, request.ownerId(), request.limit());

        if (loginUserId == null) {
            log.warn("[ClothesService] 옷 목록 조회 실패: 로그인 유저 ID 누락");
            throw new ClothesException(ErrorCode.INVALID_REQUEST);
        }

        ClothesDtoCursorRequest secureRequest = new ClothesDtoCursorRequest(
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.typeEqual(),
                loginUserId.toString(),
                request.sortBy(),
                request.sortDirection()
        );

        if (secureRequest.ownerId() == null || secureRequest.ownerId().isBlank()) {
            log.warn("[ClothesService] 옷 목록 조회 실패: ownerId 식별 불가");
            throw new ClothesException(ErrorCode.INVALID_REQUEST);
        }

        boolean hasCursor = secureRequest.cursor() != null && !secureRequest.cursor().isBlank();
        boolean hasIdAfter = secureRequest.idAfter() != null && !secureRequest.idAfter().isBlank();

        if (hasCursor ^ hasIdAfter) {
            log.warn("[ClothesService] 옷 목록 조회 실패: 불완전한 커서 정보 - cursor: {}, idAfter: {}", secureRequest.cursor(), secureRequest.idAfter());
            throw new ClothesException(ErrorCode.INVALID_INPUT_VALUE);
        }

        ClothesDtoCursorResponse response = clothesRepository.findClothesByCursor(secureRequest);

        log.info("[ClothesService] 옷 목록 커서 조회 완료 - 반환 개수: {}, hasNext: {}", response.data().size(), response.hasNext());
        return response;
    }

    @Override
    @Cacheable(value = "clothes_extraction",
            key = "T(com.ootd.fitme.infrastructure.scraper.UrlUtil).normalize(#link)",
            unless = "#result == null || #result.attributes().isEmpty()")
    public ClothesDto extractInfoFromLink(String link) {
        log.info("[ClothesService] 캐시 미스 - 스크래핑 및 AI 분석 시작: {}", link);

        ScrapedData scrapedData = scraperManager.scrape(link);

        List<Attribute> allAttributes = attributeRepository.findAll();
        String attributePromptGuide = buildAttributePromptGuide(allAttributes);

        String systemInstruction = "You are an expert fashion data analyst.\n" +
                "Read the provided product information and extract the clothing details.\n" +
                "1. name: Summarize the core product name in about 20 characters in Korean.\n" +
                "2. type: Choose EXACTLY ONE from this list: [ALL, TOP, BOTTOM, DRESS, OUTER, UNDERWEAR, SHOES, SOCKS, HAT, BAG, ACCESSORY, SCARF, ETC]\n" +
                "3. attributes: Map the product details to the [Allowed Attributes & Options] provided below.\n" +
                "   - Actively INFER attributes like gender, fit, material, and season from the context.\n" +
                "   - STRICT RULE 1: Select EXACTLY ONE value per attribute definition. DO NOT output duplicate definitionNames. (e.g., If multiple seasons apply, pick the single most dominant one).\n" +
                "   - STRICT RULE 2: For gender, infer carefully based on the brand or fit. If it's men's clothing, output '남'. If unisex or unclear, output '남녀공용' (if available) or omit it.\n" +
                "   - IMPORTANT: You MUST output the exact Korean names for the definition and value from the allowed list.\n" +
                "   - Omit unknown attributes.\n\n" +
                "[Allowed Attributes & Options]\n" + attributePromptGuide;

        String rawData = String.format("[원본 상품명]: %s\n[상세 정보]:\n%s",
                scrapedData.title(),
                scrapedData.coreText());

        AiClothesResult aiResult = null;

        try {
            aiResult = aiExtractor.extractData(rawData, systemInstruction, AiClothesResult.class);
        } catch (Exception e) {
            log.warn("[ClothesService] AI 분석 서버 응답 지연/실패. 기본 스크래핑 데이터만으로 Fallback 진행합니다. (원인: {})", e.getMessage());
        }

        String finalImageUrl = (scrapedData.imageUrl() != null && !scrapedData.imageUrl().isBlank())
                ? scrapedData.imageUrl()
                : "";

        ClothesType finalType = (aiResult != null && aiResult.type() != null) ? aiResult.type() : ClothesType.ETC;

        String finalName = (aiResult != null && aiResult.name() != null && !aiResult.name().isBlank())
                ? HtmlUtils.htmlEscape(aiResult.name())
                : HtmlUtils.htmlEscape(scrapedData.title());

        if (finalName.length() > 100) {
            finalName = finalName.substring(0, 100);
        }

        List<ClothesAttributeWithDefDto> mappedAttributes = new ArrayList<>();

        Set<String> processedDefinitions = new HashSet<>();

        if (aiResult != null && aiResult.attributes() != null) {
            for (AiClothesResult.AiAttribute aiAttr : aiResult.attributes()) {

                if (processedDefinitions.contains(aiAttr.definitionName())) {
                    continue;
                }

                allAttributes.stream()
                        .filter(dbAttr -> dbAttr.getName().equalsIgnoreCase(aiAttr.definitionName()))
                        .findFirst()
                        .ifPresent(dbAttr -> {
                            List<String> selectableOptions = dbAttr.getSelectableValues().stream()
                                    .map(sv -> sv.getType())
                                    .toList();

                            mappedAttributes.add(new ClothesAttributeWithDefDto(
                                    dbAttr.getId(),
                                    dbAttr.getName(),
                                    selectableOptions,
                                    aiAttr.value()
                            ));

                            processedDefinitions.add(aiAttr.definitionName());
                        });
            }
        }

        log.info("[ClothesService] 분석 완료 및 결과 캐싱 예정 - 최종 상품명: {}", finalName);

        return new ClothesDto(
                null,
                null,
                finalName,
                finalImageUrl,
                finalType,
                mappedAttributes
        );
    }

    // --- 내부 private 헬퍼 메서드들 ---

    private List<ClothesAttribute> buildClothesAttributes(Clothes clothes, List<ClothesAttributeDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }

        List<UUID> definitionIds = dtos.stream()
                .map(ClothesAttributeDto::definitionId)
                .toList();

        Map<UUID, Attribute> attributeMap = attributeRepository.findAllById(definitionIds).stream()
                .collect(Collectors.toMap(Attribute::getId, attr -> attr));

        List<SelectableValue> allValues = selectableValueRepository.findAllByAttributeIdIn(definitionIds);

        Map<String, SelectableValue> valueMap = allValues.stream()
                .collect(Collectors.toMap(
                        v -> v.getAttribute().getId() + "_" + v.getType(),
                        v -> v
                ));

        List<ClothesAttribute> attributes = new ArrayList<>();

        for (ClothesAttributeDto dto : dtos) {
            Attribute attributeDef = attributeMap.get(dto.definitionId());
            if (attributeDef == null) {
                log.warn("[ClothesService] 옷 속성 매핑 실패: 존재하지 않는 속성 ID - definitionId: {}", dto.definitionId());
                throw new ClothesException(ErrorCode.ATTRIBUTE_NOT_FOUND);
            }

            String compositeKey = dto.definitionId() + "_" + dto.value();
            SelectableValue selectedValue = valueMap.get(compositeKey);


            if (selectedValue == null) {
                log.warn("[ClothesService] 옷 속성 매핑 실패: 속성에 존재하지 않는 옵션 값 - definitionId: {}, value: {}", dto.definitionId(), dto.value());
                throw new ClothesException(ErrorCode.OPTION_NOT_FOUND);
            }

            ClothesAttribute newAttribute = ClothesAttribute.create(clothes, attributeDef);

            newAttribute.assignOption(selectedValue);

            attributes.add(newAttribute);
        }

        log.debug("[ClothesService] 속성 매핑 완료 - 총 매핑 수: {}", attributes.size());
        return attributes;
    }

    private List<ClothesAttributeWithDefDto> buildAttributeDtos(List<ClothesAttribute> attributes) {
        return attributes.stream()
                .filter(attr -> attr.getClothesAttributeSelectableValue() != null &&
                        attr.getClothesAttributeSelectableValue().getSelectableValue() != null)
                .map(attr -> {
                    List<String> selectableOptions = attr.getAttribute().getSelectableValues().stream()
                            .map(SelectableValue::getType)
                            .toList();

                    return new ClothesAttributeWithDefDto(
                            attr.getAttribute().getId(),
                            attr.getAttribute().getName(),
                            selectableOptions,
                            attr.getClothesAttributeSelectableValue().getSelectableValue().getType()
                    );
                }).toList();
    }

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
}


package com.ootd.fitme.domain.clothes.service;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.dto.*;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.exception.ClothesException;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.domain.selectablevalue.repository.SelectableValueRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.scraper.PlaywrightScraper;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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


    private final ImageStorage imageStorage;
    private final ApplicationEventPublisher eventPublisher;
    private final PlaywrightScraper scraper;

    @Override
    @Transactional
    public ClothesDto createClothes(ClothesCreateRequest request, MultipartFile image, UUID loginUserId) {
        if (!loginUserId.equals(request.ownerId())) {
            log.warn("[ClothesController] 타인 명의로 옷 생성 시도 차단 - loginUser: {}, requestOwner: {}", loginUserId, request.ownerId());
            throw new ClothesException(ErrorCode.AUTH_FORBIDDEN);
        }

        User user = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ClothesException(ErrorCode.CLOTHES_OWNER_NOT_FOUND));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = imageStorage.upload(image, "clothes");
        }

        String safeName = HtmlUtils.htmlEscape(request.name());

        Clothes clothes = Clothes.createWithImage(safeName, request.type(), user, imageUrl);

        List<ClothesAttribute> attributes = buildClothesAttributes(clothes, request.attributes());
        clothes.replaceAttributes(attributes);

        Clothes savedClothes = clothesRepository.save(clothes);

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
    public ClothesDto updateClothes(UUID clothesId, UUID loginUserId, ClothesUpdateRequest request, MultipartFile image) {
        Clothes clothes = clothesRepository.findByIdWithDetails(clothesId)
                .orElseThrow(() -> new ClothesException(ErrorCode.CLOTHES_NOT_FOUND));

        if (!clothes.getUser().getId().equals(loginUserId)) {
            log.warn("[ClothesService] 타인의 옷 수정 시도 차단 - clothesId: {}, loginUserId: {}", clothesId, loginUserId);
            throw new ClothesException(ErrorCode.AUTH_FORBIDDEN);
        }

        String oldImageUrl = clothes.getImageUrl();
        String imageUrl = oldImageUrl;

        if (image != null && !image.isEmpty()) {
            imageUrl = imageStorage.upload(image, "clothes");
        }

        String safeName = request.name() != null ? HtmlUtils.htmlEscape(request.name()) : clothes.getName();

        clothes.updateClothesInfo(safeName, request.type(), imageUrl);

        List<ClothesAttribute> incomingAttributes = buildClothesAttributes(clothes, request.attributes());
        clothes.updateAttributes(incomingAttributes);

        if (image != null && !image.isEmpty() && oldImageUrl != null && !oldImageUrl.isBlank()) {
            eventPublisher.publishEvent(new ImageDeleteEvent(oldImageUrl));
        }

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
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new ClothesException(ErrorCode.CLOTHES_NOT_FOUND));

        if (!clothes.getUser().getId().equals(loginUserId)) {
            log.warn("[ClothesService] 타인의 옷 삭제 시도 차단 - clothesId: {}, loginUserId: {}", clothesId, loginUserId);
            throw new ClothesException(ErrorCode.AUTH_FORBIDDEN);
        }

        String imageUrlToDelete = clothes.getImageUrl();

        clothesRepository.deleteByIdInBulk(clothesId);

        eventPublisher.publishEvent(new ImageDeleteEvent(imageUrlToDelete));
    }

    @Override
    public ClothesDtoCursorResponse getClothesList(ClothesDtoCursorRequest request, UUID loginUserId) {
        log.info("[Clothes] 옷 목록 커서 조회 요청 - ownerId: {}, limit: {}", request.ownerId(), request.limit());

        if (loginUserId == null) {
            log.warn("[ClothesService] 조회 실패: 로그인 유저 ID가 서버 내부에서 누락됨");
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
            log.warn("[Clothes] 옷 목록 조회 실패: ownerId 누락");
            throw new ClothesException(ErrorCode.INVALID_REQUEST);
        }

        boolean hasCursor = secureRequest.cursor() != null && !secureRequest.cursor().isBlank();
        boolean hasIdAfter = secureRequest.idAfter() != null && !secureRequest.idAfter().isBlank();

        if (hasCursor ^ hasIdAfter) {
            log.warn("[Clothes] 옷 목록 조회 실패: 불완전한 커서 정보 - cursor: {}, idAfter: {}", secureRequest.cursor(), secureRequest.idAfter());
            throw new ClothesException(ErrorCode.INVALID_INPUT_VALUE);
        }

        ClothesDtoCursorResponse response = clothesRepository.findClothesByCursor(secureRequest);

        log.info("[Clothes] 옷 목록 커서 조회 완료 - 반환된 아이템 수: {}, 다음 페이지 존재 여부: {}",
                response.data().size(), response.hasNext());

        return response;
    }

    @Override
    public ClothesDto extractInfoFromLink(String link) {
        log.info("[ClothesLinkService] 단순 스크래핑 시작 - URL: {}", link);

        PlaywrightScraper.ScrapedData scrapedData = scraper.scrape(link);

        String finalImageUrl = (scrapedData.imageUrl() != null && !scrapedData.imageUrl().isBlank())
                ? scrapedData.imageUrl()
                : "";
        String finalName = (scrapedData.title() != null && !scrapedData.title().isBlank())
                ? HtmlUtils.htmlEscape(scrapedData.title())
                : "이름 없음";

        if (finalName.length() > 100) {
            finalName = finalName.substring(0, 100);
        }

        log.info("[ClothesLinkService] 스크래핑 완료 - 상품명: {}", finalName);

        return new ClothesDto(
                null,                      // clothesId (아직 DB에 없으므로 null)
                null,                      // userId (마찬가지로 null)
                finalName,                 // 스크래핑한 깔끔한 상품명
                finalImageUrl,             // 스크래핑한 이미지 URL
                null,
                new ArrayList<>()          // 유저가 직접 고르도록 빈 리스트 세팅
        );
    }

    @Override
    @Transactional
    public ClothesDto createClothesFromExtracted(UUID loginUserId, ExtractedClothesInfo extractedInfo) {
        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new ClothesException(ErrorCode.CLOTHES_OWNER_NOT_FOUND));

        Clothes clothes = Clothes.createWithImage(
                extractedInfo.name(),
                extractedInfo.type(),
                user,
                extractedInfo.imageUrl()
        );

        List<ClothesAttribute> clothesAttributes = buildClothesAttributes(clothes, extractedInfo.attributes());
        clothes.replaceAttributes(clothesAttributes);

        Clothes savedClothes = clothesRepository.save(clothes);

        List<ClothesAttributeWithDefDto> attributeDtos = buildAttributeDtos(clothesAttributes);

        log.info("[ClothesLinkService] DB 저장 완료 - ID: {}", savedClothes.getId());

        return new ClothesDto(
                savedClothes.getId(),
                user.getId(),
                savedClothes.getName(),
                savedClothes.getImageUrl(),
                savedClothes.getClothesType(),
                attributeDtos
        );
    }

    // --- 내부 private 헬퍼 메서드들 ---

    private List<ClothesAttribute> buildClothesAttributes(Clothes clothes, List<ClothesAttributeDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return new ArrayList<>();

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
                throw new ClothesException(ErrorCode.ATTRIBUTE_NOT_FOUND);
            }

            String compositeKey = dto.definitionId() + "_" + dto.value();
            SelectableValue selectedValue = valueMap.get(compositeKey);

            if (selectedValue == null) {
                throw new ClothesException(ErrorCode.OPTION_NOT_FOUND);
            }

            ClothesAttribute newAttribute = ClothesAttribute.create(clothes, attributeDef);
            newAttribute.assignOption(selectedValue);

            attributes.add(newAttribute);
        }

        return attributes;
    }

    private List<ClothesAttributeWithDefDto> buildAttributeDtos(List<ClothesAttribute> attributes) {
        return attributes.stream()
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
}

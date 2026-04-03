package com.ootd.fitme.domain.clothes.service;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.dto.ClothesAttributeDto;
import com.ootd.fitme.domain.clothes.dto.ClothesAttributeWithDefDto;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    @Override
    @Transactional
    public ClothesDto createClothes(ClothesCreateRequest request, MultipartFile image) {
        User user = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ClothesException(ErrorCode.CLOTHES_OWNER_NOT_FOUND));

        String imageUrl = null;
//        if (image != null && !image.isEmpty()) {
//            imageUrl = imageUploadService.uploadImage(image);
//        }

        String safeName = HtmlUtils.htmlEscape(request.name());

        Clothes clothes = Clothes.createWithImage(safeName, request.type(), user, imageUrl);

        List<ClothesAttribute> attributes = buildClothesAttributes(clothes, request.attributes());
        clothes.replaceAttributes(attributes);

        Clothes savedClothes = clothesRepository.save(clothes);

        List<ClothesAttributeWithDefDto> attributeDtos = attributes.stream()
                .map(attr -> new ClothesAttributeWithDefDto(
                        attr.getAttribute().getId(),
                        attr.getAttribute().getName(),
                        null,
                        attr.getClothesAttributeSelectableValue().getSelectableValue().getType()
                )).toList();

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

        String imgUrl = null;
//        if (image != null && !image.isEmpty()) {
//            String newImageUrl = imageUploadService.uploadImage(image);
//            // clothes.updateImage(newImageUrl);
//        }

        String safeName = request.name() != null ? HtmlUtils.htmlEscape(request.name()) : clothes.getName();

        clothes.updateClothesInfo(safeName, request.type(), imgUrl);

        List<ClothesAttribute> incomingAttributes = buildClothesAttributes(clothes, request.attributes());
        clothes.updateAttributes(incomingAttributes);

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

        // 🌟 서비스 내부 권한 검증
        if (!clothes.getUser().getId().equals(loginUserId)) {
            log.warn("[ClothesService] 타인의 옷 삭제 시도 차단 - clothesId: {}, loginUserId: {}", clothesId, loginUserId);
            throw new ClothesException(ErrorCode.AUTH_FORBIDDEN);
        }

        clothesRepository.deleteByIdInBulk(clothesId);
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
                response.contents().size(), response.hasNext());

        return response;
    }

    @Override
    public Object extractInfoFromLink(String link) {
        return null;
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

            String compositeKey = dto.definitionId() + "_" + dto.type();
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
                .map(attr -> new ClothesAttributeWithDefDto(
                        attr.getAttribute().getId(),
                        attr.getAttribute().getName(),
                        null,
                        attr.getClothesAttributeSelectableValue().getSelectableValue().getType()
                )).toList();
    }

}

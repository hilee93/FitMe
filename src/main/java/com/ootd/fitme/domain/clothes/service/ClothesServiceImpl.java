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

        Clothes clothes = Clothes.createWithImage(request.name(), request.type(), user, imageUrl);

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
    public ClothesDto updateClothes(UUID clothesId, ClothesUpdateRequest request, MultipartFile image) {
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new ClothesException(ErrorCode.CLOTHES_NOT_FOUND));



        String imgUrl = null;
//        if (image != null && !image.isEmpty()) {
//            String newImageUrl = imageUploadService.uploadImage(image);
//            // clothes.updateImage(newImageUrl);
//        }

        clothes.updateClothesInfo(request.name(), request.type(), imgUrl);

        List<ClothesAttribute> incomingAttributes = buildClothesAttributes(clothes, request.attributes());
        clothes.updateAttributes(incomingAttributes);

        List<ClothesAttributeWithDefDto> updatedAttributeDtos = request.attributes().stream()
                .map(reqAttr -> new ClothesAttributeWithDefDto(
                        reqAttr.definitionId(),
                        attributeRepository.getReferenceById(reqAttr.definitionId()).getName(),
                        null,
                        reqAttr.type()
                )).toList();

        return new ClothesDto(
                clothes.getId(),
                clothes.getUser().getId(),
                request.name() != null ? request.name() : clothes.getName(),
                clothes.getImageUrl(),
                request.type() != null ? request.type() : clothes.getClothesType(),
                updatedAttributeDtos
        );
    }

    @Override
    @Transactional
    public void deleteClothes(UUID clothesId) {
        boolean exists = clothesRepository.existsById(clothesId);
        if (!exists) {
            throw new ClothesException(ErrorCode.CLOTHES_NOT_FOUND);
        }
        clothesRepository.deleteByIdInBulk(clothesId);
    }

    @Override
    public ClothesDtoCursorResponse getClothesList(ClothesDtoCursorRequest request) {
        // QueryDSL 등을 활용한 커서 기반 페이징 로직 구현
        return null;
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

}

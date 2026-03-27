package com.ootd.fitme.domain.attribute.service;

import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;
import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.exception.AttributeException;
import com.ootd.fitme.domain.attribute.mapper.AttributeMapper;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttributeDefServiceImpl implements AttributeDefService {

    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;

    @Override
    public List<ClothesAttributeDefDto> getClothesAttributeDefs(String sortBy, String sortDirection, String keywordLike) {
        List<Attribute> attributes = attributeRepository.findAttributesWithCondition(sortBy, sortDirection, keywordLike);
        return attributes.stream()
                .map(attributeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ClothesAttributeDefDto createClothesAttributeDef(ClothesAttributeDefCreateRequest request) {
        validateDuplicateName(request.name());

        Attribute attribute = Attribute.create(request.name());
        attribute.addValues(request.selectableValues());

        Attribute savedAttribute = attributeRepository.save(attribute);
        log.info("신규 의상 속성 정의 생성 완료 - ID: {}", savedAttribute.getId());

        return attributeMapper.toDto(savedAttribute);
    }

    @Override
    @Transactional
    public ClothesAttributeDefDto updateClothesAttributeDef(UUID definitionId, ClothesAttributeDefUpdateRequest request) {
        Attribute attribute = getAttributeOrThrow(definitionId);

        if (!attribute.getName().equals(request.name())) {
            validateDuplicateName(request.name());
        }

        attribute.updateAttribute(request.name(), request.selectableValues());

        log.info("의상 속성 정의 수정 완료 - ID: {}", attribute.getId());
        return attributeMapper.toDto(attribute);
    }

    @Override
    @Transactional
    public void deleteClothesAttributeDef(UUID definitionId) {
        Attribute attribute = getAttributeOrThrow(definitionId);
        attributeRepository.delete(attribute);
        log.info("의상 속성 정의 삭제 완료 - ID: {}", definitionId);
    }

    private Attribute getAttributeOrThrow(UUID id) {
        return attributeRepository.findById(id)
                .orElseThrow(() -> new AttributeException(ErrorCode.ATTRIBUTE_NOT_FOUND));
    }

    private void validateDuplicateName(String name) {
        if (attributeRepository.existsByName(name)) {
            throw new AttributeException(ErrorCode.ATTRIBUTE_NAME_DUPLICATED);
        }
    }
}

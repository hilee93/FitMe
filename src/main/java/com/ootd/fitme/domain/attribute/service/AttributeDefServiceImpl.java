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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Cacheable(value = "attributes", key = "#sortBy + '_' + #sortDirection + '_' + #keywordLike")
    public List<ClothesAttributeDefDto> getClothesAttributeDefs(String sortBy, String sortDirection, String keywordLike) {
        log.info("[AttributeService] 속성 목록 DB 조회 실행 - sortBy: {}, direction: {}, keyword: {}", sortBy, sortDirection, keywordLike);

        List<Attribute> attributes = attributeRepository.findAttributesWithCondition(sortBy, sortDirection, keywordLike);

        log.info("[AttributeService] 속성 목록 DB 조회 완료 - 반환 개수: {}", attributes.size());

        return attributes.stream()
                .map(attributeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "attributes", allEntries = true)
    public ClothesAttributeDefDto createClothesAttributeDef(ClothesAttributeDefCreateRequest request) {
        log.info("[AttributeService] 속성 생성 요청 - name: {}, 옵션 개수: {}", request.name(), request.selectableValues().size());

        validateDuplicateName(request.name());

        Attribute attribute = Attribute.create(request.name());
        attribute.addValues(request.selectableValues());

        Attribute savedAttribute = attributeRepository.save(attribute);
        log.info("[AttributeService] 속성 생성 완료 (캐시 초기화됨) - attributeId: {}, name: {}", savedAttribute.getId(), savedAttribute.getName());

        return attributeMapper.toDto(savedAttribute);
    }

    @Override
    @Transactional
    @CacheEvict(value = "attributes", allEntries = true)
    public ClothesAttributeDefDto updateClothesAttributeDef(UUID definitionId, ClothesAttributeDefUpdateRequest request) {
        log.info("[AttributeService] 속성 수정 요청 - attributeId: {}, newName: {}, 옵션 개수: {}", definitionId, request.name(), request.selectableValues().size());

        Attribute attribute = getAttributeOrThrow(definitionId);

        if (!attribute.getName().equals(request.name())) {
            validateDuplicateName(request.name());
        }

        attribute.updateAttribute(request.name(), request.selectableValues());

        log.info("[AttributeService] 속성 수정 완료 (캐시 초기화됨) - attributeId: {}", attribute.getId());
        return attributeMapper.toDto(attribute);
    }

    @Override
    @Transactional
    @CacheEvict(value = "attributes", allEntries = true)
    public void deleteClothesAttributeDef(UUID definitionId) {
        log.info("[AttributeService] 속성 삭제 요청 - attributeId: {}", definitionId);

        Attribute attribute = getAttributeOrThrow(definitionId);
        attributeRepository.deleteByIdInBulk(definitionId);

        log.info("[AttributeService] 속성 삭제 완료 (캐시 초기화됨) - attributeId: {}", definitionId);
    }

    // --- 내부 헬퍼 메서드들 ---

    private Attribute getAttributeOrThrow(UUID id) {
        return attributeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[AttributeService] 검증 실패: 존재하지 않는 속성 ID - attributeId: {}", id);
                    return new AttributeException(ErrorCode.ATTRIBUTE_NOT_FOUND);
                });
    }

    private void validateDuplicateName(String name) {
        if (attributeRepository.existsByName(name)) {
            log.warn("[AttributeService] 검증 실패: 이미 존재하는 속성명 - name: {}", name);
            throw new AttributeException(ErrorCode.ATTRIBUTE_NAME_DUPLICATED);
        }
    }
}
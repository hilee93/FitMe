package com.ootd.fitme.domain.attribute.service;

import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;

import java.util.List;
import java.util.UUID;

public interface AttributeDefService {
    // 의상 속성 정의 목록 조회
    List<ClothesAttributeDefDto> getClothesAttributeDefs(String sortBy, String sortDirection, String keywordLike);

    // 의상 속성 정의 등록
    ClothesAttributeDefDto createClothesAttributeDef(ClothesAttributeDefCreateRequest request);

    // 의상 속성 정의 수정
    ClothesAttributeDefDto updateClothesAttributeDef(UUID definitionId, ClothesAttributeDefUpdateRequest request);

    // 의상 속성 정의 삭제
    void deleteClothesAttributeDef(UUID definitionId);
}

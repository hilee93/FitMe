package com.ootd.fitme.domain.attribute.controller.docs;

import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "의상 속성 정의", description = "의상 속성 정의 관련 API")
public interface AttributeDefControllerDocs {
    @Operation(summary = "의상 속성 정의 목록 조회", description = "의상 속성 정의 목록 조회 API")
    @GetMapping
    ResponseEntity<List<ClothesAttributeDefDto>> getClothesAttributeDefs(
            @Parameter(description = "정렬 기준 (createdAt, name)", required = true) @RequestParam("sortBy") String sortBy,
            @Parameter(description = "정렬 방향 (ASCENDING, DESCENDING)", required = true) @RequestParam("sortDirection") String sortDirection,
            @Parameter(description = "검색 키워드") @RequestParam(value = "keywordLike", required = false) String keywordLike
    );

    @Operation(summary = "의상 속성 정의 등록", description = "의상 속성 정의 등록 API")
    @PostMapping
    ResponseEntity<ClothesAttributeDefDto> createClothesAttributeDef(
            @RequestBody ClothesAttributeDefCreateRequest request
    );

    @Operation(summary = "의상 속성 정의 수정", description = "의상 속성 정의 수정 API")
    @PatchMapping("/{definitionId}")
    ResponseEntity<ClothesAttributeDefDto> updateClothesAttributeDef(
            @Parameter(description = "속성 정의 ID", required = true) @PathVariable("definitionId") UUID definitionId,
            @RequestBody ClothesAttributeDefUpdateRequest request
    );

    @Operation(summary = "의상 속성 정의 삭제", description = "의상 속성 정의 삭제 API")
    @DeleteMapping("/{definitionId}")
    ResponseEntity<Void> deleteClothesAttributeDef(
            @Parameter(description = "속성 정의 ID", required = true) @PathVariable("definitionId") UUID definitionId
    );
}

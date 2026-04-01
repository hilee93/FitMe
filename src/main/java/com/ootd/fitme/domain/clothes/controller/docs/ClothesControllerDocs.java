package com.ootd.fitme.domain.clothes.controller.docs;

import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Tag(name = "의상 관리", description = "옷장 프로젝트 의상 관리 API")
public interface ClothesControllerDocs {

    @Operation(summary = "옷 목록 조회", description = "옷 목록을 Cursor 기반으로 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "옷 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    ResponseEntity<ClothesDtoCursorResponse> getClothes(
            @RequestBody ClothesDtoCursorRequest request
    );

    @Operation(summary = "옷 등록", description = "새로운 옷과 이미지를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "옷 등록 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    ResponseEntity<ClothesDto> createClothes(
            @Parameter(description = "옷 등록 정보", required = true) ClothesCreateRequest request,
            @Parameter(description = "옷 이미지 파일") MultipartFile image
    );

    @Operation(summary = "옷 삭제", description = "특정 ID의 옷을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "옷 삭제 성공")
    ResponseEntity<Void> deleteClothes(
            @Parameter(description = "삭제할 옷의 UUID", required = true) @PathVariable UUID clothesId
    );

    @Operation(summary = "옷 수정", description = "특정 ID의 옷 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "옷 수정 성공")
    ResponseEntity<ClothesDto> updateClothes(
            @Parameter(description = "수정할 옷의 UUID", required = true) @PathVariable UUID clothesId,
            @Parameter(description = "수정할 옷의 정보") @ModelAttribute ClothesUpdateRequest request,
            @Parameter(description = "수정할 옷의 사진") @RequestPart MultipartFile image
    );

    @Operation(summary = "구매 링크로 옷 정보 불러오기", description = "상품 링크를 통해 옷의 상세 정보를 추출합니다.")
    @ApiResponse(responseCode = "200", description = "정보 추출 성공")
    ResponseEntity<?> extractClothesInfo(
            @Parameter(description = "상품 구매 링크", required = true) @RequestParam String link
    );
}

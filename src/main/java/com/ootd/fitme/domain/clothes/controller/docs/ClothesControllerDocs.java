package com.ootd.fitme.domain.clothes.controller.docs;

import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "의상 관리", description = "옷장 프로젝트 의상 관리 API")
public interface ClothesControllerDocs {

    @Operation(summary = "옷 목록 조회", description = "로그인한 유저 본인의 옷 목록을 Cursor 기반으로 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "옷 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 페이징 파라미터 요청")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    ResponseEntity<ClothesDtoCursorResponse> getClothes(
            @ModelAttribute ClothesDtoCursorRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @Operation(summary = "옷 등록", description = "새로운 옷과 이미지를 등록합니다. (Multipart Form-Data 형식)")
    @ApiResponse(responseCode = "201", description = "옷 등록 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    @ApiResponse(responseCode = "403", description = "권한 없음 (타인 명의로 등록 시도)")
    ResponseEntity<ClothesDto> createClothes(
            @Parameter(description = "옷 등록 정보 (JSON 형식)", required = true) @RequestPart("request") ClothesCreateRequest request,
            @Parameter(description = "옷 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @Operation(summary = "옷 삭제", description = "로그인한 유저 본인 소유의 특정 ID 옷을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "옷 삭제 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음 (타인의 옷 삭제 시도)")
    @ApiResponse(responseCode = "404", description = "해당 옷을 찾을 수 없음")
    ResponseEntity<Void> deleteClothes(
            @Parameter(description = "삭제할 옷의 UUID", required = true) @PathVariable UUID clothesId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @Operation(summary = "옷 수정", description = "특정 ID의 옷 정보를 수정합니다. (Multipart Form-Data 형식)")
    @ApiResponse(responseCode = "200", description = "옷 수정 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음 (타인의 옷 수정 시도)")
    @ApiResponse(responseCode = "404", description = "해당 옷을 찾을 수 없음")
    ResponseEntity<ClothesDto> updateClothes(
            @Parameter(description = "수정할 옷의 UUID", required = true) @PathVariable UUID clothesId,
            @Parameter(description = "수정할 옷의 정보") @ModelAttribute ClothesUpdateRequest request,
            @Parameter(description = "수정할 옷의 사진") @RequestPart(value = "image", required = false) MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @Operation(summary = "구매 링크로 옷 정보 불러오기", description = "상품 링크를 통해 옷의 상세 정보를 추출합니다.")
    @ApiResponse(responseCode = "200", description = "정보 추출 성공")
    ResponseEntity<?> extractClothesInfo(
            @Parameter(description = "상품 구매 링크", required = true) @RequestParam String link
    );
}

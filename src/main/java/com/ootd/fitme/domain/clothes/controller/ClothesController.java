package com.ootd.fitme.domain.clothes.controller;

import com.ootd.fitme.domain.clothes.controller.docs.ClothesControllerDocs;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.exception.ClothesException;
import com.ootd.fitme.domain.clothes.service.ClothesService;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 프로젝트에 맞게 변경 필요
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothesController implements ClothesControllerDocs {

    private final ClothesService clothesService;

    @GetMapping
    public ResponseEntity<ClothesDtoCursorResponse> getClothes(
            @ModelAttribute ClothesDtoCursorRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UUID loginUserId = principal.getUserId();
        ClothesDtoCursorResponse response = clothesService.getClothesList(request, loginUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClothesDto> createClothes(
            @RequestPart("request") ClothesCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UUID loginUserId = principal.getUserId();
        if (!loginUserId.equals(request.ownerId())) {
            log.warn("[ClothesController] 타인 명의로 옷 생성 시도 차단 - loginUser: {}, requestOwner: {}", loginUserId, request.ownerId());
            throw new ClothesException(ErrorCode.AUTH_FORBIDDEN);
        }

        ClothesDto response = clothesService.createClothes(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> deleteClothes(
            @PathVariable UUID clothesId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UUID loginUserId = principal.getUserId();
        clothesService.deleteClothes(clothesId, loginUserId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{clothesId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClothesDto> updateClothes(
            @PathVariable UUID clothesId,
            @ModelAttribute ClothesUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UUID loginUserId = principal.getUserId();
        ClothesDto response = clothesService.updateClothes(clothesId, loginUserId, request, image);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/extractions")
    public ResponseEntity<?> extractClothesInfo(@RequestParam String link) {
        Object response = clothesService.extractInfoFromLink(link);
        return ResponseEntity.ok(response);
    }
}

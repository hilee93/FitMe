package com.ootd.fitme.domain.clothes.controller;

import com.ootd.fitme.domain.clothes.controller.docs.ClothesControllerDocs;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.service.ClothesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothesController implements ClothesControllerDocs {

    private final ClothesService clothesService;

    @GetMapping
    public ResponseEntity<ClothesDtoCursorResponse> getClothes(
            @ModelAttribute ClothesDtoCursorRequest request
    ) {

        ClothesDtoCursorResponse response = clothesService.getClothesList(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ClothesDto> createClothes(
            @RequestPart("request") ClothesDtoCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
            ) {

        ClothesDto response = clothesService.createClothes(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> deleteClothes(@PathVariable UUID clothesId) {
        clothesService.deleteClothes(clothesId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{clothesId}")
    public ResponseEntity<ClothesDto> updateClothes(
            @PathVariable UUID clothesId
            // TODO: 수정 시 필요한 Request DTO 추가
    ) {
        ClothesDto response = clothesService.updateClothes(clothesId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/extractions")
    public ResponseEntity<?> extractClothesInfo(@RequestParam String link) {
        // TODO: 응답 DTO 설계 후 반환 타입 변경
        Object response = clothesService.extractInfoFromLink(link);
        return ResponseEntity.ok(response);
    }
}
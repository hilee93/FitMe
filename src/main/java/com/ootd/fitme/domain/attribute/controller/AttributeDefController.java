package com.ootd.fitme.domain.attribute.controller;

import com.ootd.fitme.domain.attribute.controller.docs.AttributeDefControllerDocs;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;
import com.ootd.fitme.domain.attribute.service.AttributeDefService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clothes/attribute-defs")
@RequiredArgsConstructor
public class AttributeDefController implements AttributeDefControllerDocs {
    private final AttributeDefService service;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public ResponseEntity<List<ClothesAttributeDefDto>> getClothesAttributeDefs(String sortBy, String sortDirection, String keywordLike) {
        List<ClothesAttributeDefDto> response = service.getClothesAttributeDefs(sortBy, sortDirection, keywordLike);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<ClothesAttributeDefDto> createClothesAttributeDef(ClothesAttributeDefCreateRequest request) {
        ClothesAttributeDefDto response = service.createClothesAttributeDef(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<ClothesAttributeDefDto> updateClothesAttributeDef(UUID definitionId, ClothesAttributeDefUpdateRequest request) {
        ClothesAttributeDefDto response = service.updateClothesAttributeDef(definitionId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<Void> deleteClothesAttributeDef(UUID definitionId) {
        service.deleteClothesAttributeDef(definitionId);
        return ResponseEntity.noContent().build();
    }
}

package com.ootd.fitme.domain.clothes.service;

import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ClothesService {
    ClothesDtoCursorResponse getClothesList(ClothesDtoCursorRequest request, UUID loginUserId);
    ClothesDto createClothes(ClothesCreateRequest request, MultipartFile image);
    void deleteClothes(UUID clothesId, UUID loginUserId);
    ClothesDto updateClothes(UUID clothesId, UUID loginUserId, ClothesUpdateRequest request, MultipartFile image);
    Object extractInfoFromLink(String link);
}

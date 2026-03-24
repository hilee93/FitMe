package com.ootd.fitme.domain.clothes.service;

import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ClothesService {
    ClothesDtoCursorResponse getClothesList(ClothesDtoCursorRequest request);
    ClothesDto createClothes(ClothesDtoCreateRequest request, MultipartFile image);
    void deleteClothes(UUID clothesId);
    ClothesDto updateClothes(UUID clothesId);
    Object extractInfoFromLink(String link);
}

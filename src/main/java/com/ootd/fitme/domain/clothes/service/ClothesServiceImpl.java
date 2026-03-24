package com.ootd.fitme.domain.clothes.service;

import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class ClothesServiceImpl implements ClothesService{
    private final ClothesRepository clothesRepository;

    @Override
    public ClothesDtoCursorResponse getClothesList(ClothesDtoCursorRequest request) {
        return null;
    }

    @Override
    public ClothesDto createClothes(ClothesDtoCreateRequest request, MultipartFile image) {
        return null;
    }

    @Override
    public void deleteClothes(UUID clothesId) {

    }

    @Override
    public ClothesDto updateClothes(UUID clothesId) {
        return null;
    }

    @Override
    public ClothesDto extractInfoFromLink(String link) {
        return null;
    }
}

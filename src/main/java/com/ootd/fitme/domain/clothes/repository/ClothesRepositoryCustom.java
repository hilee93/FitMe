package com.ootd.fitme.domain.clothes.repository;

import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;

public interface ClothesRepositoryCustom {
    ClothesDtoCursorResponse findClothesByCursor(ClothesDtoCursorRequest request);
}

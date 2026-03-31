package com.ootd.fitme.domain.profile.service;

import com.ootd.fitme.domain.profile.dto.request.ProfileUpdateRequest;
import com.ootd.fitme.domain.profile.dto.response.ProfileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProfileService {
    ProfileDto getProfile(UUID userId);
    ProfileDto updateProfile(UUID userId, ProfileUpdateRequest request, MultipartFile image);
}

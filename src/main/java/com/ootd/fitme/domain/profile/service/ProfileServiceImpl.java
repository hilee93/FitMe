package com.ootd.fitme.domain.profile.service;

import com.ootd.fitme.domain.profile.dto.request.ProfileUpdateRequest;
import com.ootd.fitme.domain.profile.dto.response.ProfileDto;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.exception.ProfileException;
import com.ootd.fitme.domain.profile.mapper.ProfileMapper;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.region.service.RegionService;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.exception.StorageException;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {
    private static final long MAX_PROFILE_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif",
            ".bmp", ".avif", ".heic", ".heif"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpg", "image/jpeg", "image/png",
            "image/webp", "image/gif", "image/bmp",
            "image/avif", "image/heic", "image/heif"
    );

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;
    private final RegionService regionService;
    private final ImageStorage imageStorage;

    @Override
    public ProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileException(ErrorCode.PROFILE_NOT_FOUND));

        return profileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public ProfileDto updateProfile(UUID userId,
                                    ProfileUpdateRequest request,
                                    MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileException(ErrorCode.PROFILE_NOT_FOUND));

        ProfileUpdateRequest normalizedRequest = normalizeLocation(request);
        profileMapper.apply(profile, normalizedRequest);

        if (image != null && !image.isEmpty()) {
            String originalFilename = image.getOriginalFilename();
            if (originalFilename != null && !originalFilename.isBlank()) {
                String oldImageUrl = profile.getProfileImageUrl();
                String newImageUrl = uploadProfileImage(image);
                profile.updateProfileImageUrl(newImageUrl);
                deleteOldImageQuietly(oldImageUrl, newImageUrl);
            }
        }
        return profileMapper.toDto(profile);
    }

    private String uploadProfileImage(MultipartFile image) {
        validateImage(image);
        try {
            String imageUrl = imageStorage.upload(image, "profile");
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new StorageException(ErrorCode.FILE_UPLOAD_FAILED);
            }
            return imageUrl;
        } catch (StorageException e) {
            log.error("Failed to upload profile image. filename={}, contentType={}",
                    image.getOriginalFilename(), image.getContentType(), e);
            throw new ProfileException(ErrorCode.PROFILE_IMAGE_SAVE_FAILED);
        }
    }

    private void deleteOldImageQuietly(String oldImageUrl, String newImageUrl) {
        if (oldImageUrl == null || oldImageUrl.isBlank()) {
            return;
        }

        if (oldImageUrl.equals(newImageUrl)) {
            return;
        }

        try {
            imageStorage.delete(oldImageUrl);
        } catch (Exception e) {
            log.warn("Old profile image delete failed: {}", oldImageUrl);
        }
    }

    private ProfileUpdateRequest normalizeLocation(ProfileUpdateRequest request) {
        if (request == null || request.location() == null) {
            return request;
        }

        WeatherAPILocation location = request.location();
        if (location.latitude() == null || location.longitude() == null) {
            throw new ProfileException(ErrorCode.INVALID_INPUT_VALUE);
        }

        WeatherAPILocation resolvedLocation =
                regionService.resolveLocation(location.longitude(), location.latitude());

        return new ProfileUpdateRequest(
                request.name(),
                request.gender(),
                request.birthDate(),
                resolvedLocation,
                request.temperatureSensitivity()
        );
    }

    private void validateImage(MultipartFile image) {
        if (image.getSize() > MAX_PROFILE_IMAGE_SIZE_BYTES) {
            throw new ProfileException(ErrorCode.PROFILE_IMAGE_TOO_LARGE);
        }

        String contentType = image.getContentType();
        if (contentType != null) {
            contentType = contentType.toLowerCase(Locale.ROOT).trim();
            int separatorIndex = contentType.indexOf(';');
            if (separatorIndex >= 0) {
                contentType = contentType.substring(0, separatorIndex).trim();
            }
        }

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ProfileException(ErrorCode.PROFILE_IMAGE_CONTENT_TYPE_NOT_ALLOWED);
        }

        String originalFilename = image.getOriginalFilename();
        String extension = "";
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
            }
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ProfileException(ErrorCode.PROFILE_IMAGE_EXTENSION_NOT_ALLOWED);
        }
    }
}

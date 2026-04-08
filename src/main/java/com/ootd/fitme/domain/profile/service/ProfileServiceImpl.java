package com.ootd.fitme.domain.profile.service;

import com.ootd.fitme.domain.profile.dto.request.ProfileUpdateRequest;
import com.ootd.fitme.domain.profile.dto.response.ProfileDto;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.exception.ProfileException;
import com.ootd.fitme.domain.profile.mapper.ProfileMapper;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/*
TODO: 책임 분리를 위해 헬퍼 메서드 다른 파일로 이관 예정
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {
    private static final Path PROFILE_IMAGE_PATH = Paths.get("uploads/profile");
    private static final long MAX_PROFILE_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    @Override
    public ProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Profile profile = findRequiredProfile(user.getId());

        return profileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public ProfileDto updateProfile(UUID userId,
                                    ProfileUpdateRequest request,
                                    MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Profile profile = findRequiredProfile(user.getId());

        profileMapper.apply(profile, request);

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(userId, image);
            profile.updateProfileImageUrl(imageUrl);
        }
        return profileMapper.toDto(profile);
    }

    private Profile findRequiredProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileException(ErrorCode.PROFILE_NOT_FOUND));
    }

    private String saveImage(UUID userId, MultipartFile image) {
        validateImage(image);
        try {
            Files.createDirectories(PROFILE_IMAGE_PATH);
            String extension = extractExtension(image.getOriginalFilename());
            String fileName = userId + "-" + UUID.randomUUID() + extension;
            Path target = PROFILE_IMAGE_PATH.resolve(fileName).normalize();
            image.transferTo(target.toFile());
            return "/uploads/profile/" + fileName;
        } catch (Exception e) {
            throw new ProfileException(ErrorCode.PROFILE_IMAGE_SAVE_FAILED);
        }
    }

    private void validateImage(MultipartFile image) {
        if (image.getSize() > MAX_PROFILE_IMAGE_SIZE_BYTES) {
            throw new ProfileException(ErrorCode.PROFILE_IMAGE_SAVE_FAILED);
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ProfileException(ErrorCode.PROFILE_IMAGE_CONTENT_TYPE_NOT_ALLOWED);
        }

        String extension = extractExtension(image.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ProfileException(ErrorCode.PROFILE_IMAGE_EXTENSION_NOT_ALLOWED);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    }
}

package com.ootd.fitme.domain.mediafile.service;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.exception.MediaFileException;
import com.ootd.fitme.domain.mediafile.repository.MediaFileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaFileService {

    private final ImageStorage imageStorage;
    private final MediaFileRepository mediaFileRepository;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".avif", ".heic", ".heif"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpg", "image/jpeg", "image/png", "image/webp",
            "image/gif", "image/bmp", "image/avif", "image/heic", "image/heif"
    );

    @Transactional
    public String uploadAndRegister(MultipartFile file, MediaPurpose purpose, User user) {
        validateImageFile(file);
        log.info("[MediaFileService] 미디어 업로드 및 등록 시작 - fileName: {}, purpose: {}, userId: {}",
                file.getOriginalFilename(), purpose, user.getId());
        String uploadedUrl = imageStorage.upload(file, purpose.name().toLowerCase());
        log.debug("[MediaFileService] 외부 스토리지 업로드 완료 - URL: {}", uploadedUrl);

        MediaFile mediaFile = MediaFile.create(uploadedUrl, file.getOriginalFilename(), purpose, user);
        mediaFileRepository.save(mediaFile);

        log.info("[MediaFileService] 미디어 DB 등록 완료 - mediaFileId: {}, URL: {}", mediaFile.getId(), uploadedUrl);

        return uploadedUrl;
    }

    @Transactional
    public void deleteMedia(String fileUrl, UUID loginUserId) {
        log.info("[MediaFileService] 미디어 삭제 요청 시작 - fileUrl: {}, loginUserId: {}", fileUrl, loginUserId);
        MediaFile mediaFile = mediaFileRepository.findByFileUrl(fileUrl)
                .orElseThrow(() -> {
                    log.warn("[MediaFileService] 삭제 실패: 존재하지 않는 미디어 파일 - URL: {}", fileUrl);
                    return new MediaFileException(ErrorCode.MEDIA_FILE_NOT_FOUND);
                });

        if (!mediaFile.isOwner(loginUserId)) {
            log.warn("[MediaFileService] 보안 경고: 타인의 미디어 파일 삭제 시도 - fileUrl: {}, loginUserId: {}, ownerId: {}",
                    fileUrl, loginUserId, mediaFile.getUser().getId());
            throw new MediaFileException(ErrorCode.MEDIA_FILE_ACCESS_DENIED);
        }
        mediaFile.markAsPendingDelete();
        log.info("[MediaFileService] 미디어 파일 삭제 대기 상태로 변경 완료 - URL: {}", fileUrl);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MediaFileException(ErrorCode.INVALID_FILE_REQUEST); // 예: "파일이 비어있습니다."
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        // 1. Content-Type 검증
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("[보안] 허용되지 않은 Content-Type 업로드 시도: {}", contentType);
            throw new MediaFileException(ErrorCode.UNSUPPORTED_FILE_FORMAT);
        }

        // 2. 확장자 검증
        if (originalFilename == null) {
            throw new MediaFileException(ErrorCode.INVALID_FILE_REQUEST);
        }

        String extension = extractExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("[보안] 허용되지 않은 파일 확장자 업로드 시도: {}", extension);
            throw new MediaFileException(ErrorCode.UNSUPPORTED_FILE_FORMAT);
        }
    }

    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
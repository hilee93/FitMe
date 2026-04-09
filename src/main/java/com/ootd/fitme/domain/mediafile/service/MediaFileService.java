package com.ootd.fitme.domain.mediafile.service;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.event.FileDeleteEvent;
import com.ootd.fitme.domain.mediafile.exception.MediaFileException;
import com.ootd.fitme.domain.mediafile.repository.MediaFileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaFileService {

    private final ImageStorage imageStorage;
    private final MediaFileRepository mediaFileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public String uploadAndRegister(MultipartFile file, MediaPurpose purpose, User user) {
        String uploadedUrl = imageStorage.upload(file, purpose.name().toLowerCase());

        MediaFile mediaFile = MediaFile.create(uploadedUrl, file.getOriginalFilename(), purpose, user);
        mediaFileRepository.save(mediaFile);

        return uploadedUrl;
    }

    @Transactional
    public void deleteMedia(String fileUrl, UUID loginUserId) {
        MediaFile mediaFile = mediaFileRepository.findByFileUrl(fileUrl)
                .orElseThrow(() -> new MediaFileException(ErrorCode.MEDIA_FILE_NOT_FOUND));

        if (!mediaFile.isOwner(loginUserId)) {
            throw new MediaFileException(ErrorCode.MEDIA_FILE_ACCESS_DENIED);
        }

        eventPublisher.publishEvent(new FileDeleteEvent(fileUrl));
    }
}
package com.ootd.fitme.domain.mediafile.repository;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {
    Optional<MediaFile> findByFileUrl(String fileUrl);

    void deleteByFileUrl(String fileUrl);

    List<MediaFile> findByStatus(MediaStatus status);
}

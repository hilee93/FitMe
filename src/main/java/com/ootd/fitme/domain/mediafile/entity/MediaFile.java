package com.ootd.fitme.domain.mediafile.entity;

import com.ootd.fitme.domain.base.BaseEntity;
import com.ootd.fitme.domain.base.BaseUpdateEntity;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.enums.MediaStatus;
import com.ootd.fitme.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "media_files")
public class MediaFile extends BaseUpdateEntity {

    @Column(nullable = false, unique = true, length = 1000)
    private String fileUrl;

    @Column(nullable = false)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private MediaFile(String fileUrl, String originalFileName, MediaPurpose purpose, User user) {
        this.fileUrl = fileUrl;
        this.originalFileName = originalFileName;
        this.purpose = purpose;
        this.user = user;
        this.status = MediaStatus.ACTIVE;
    }

    public static MediaFile create(String fileUrl, String originalFileName, MediaPurpose purpose, User user) {
        return new MediaFile(fileUrl, originalFileName, purpose, user);
    }

    public void markAsPendingDelete() {
        this.status = MediaStatus.PENDING_DELETE;
    }

    public boolean isOwner(UUID loginUserId) {
        return this.user.getId().equals(loginUserId);
    }
}


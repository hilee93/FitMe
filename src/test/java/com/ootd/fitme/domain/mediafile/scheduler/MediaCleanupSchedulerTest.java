package com.ootd.fitme.domain.mediafile.scheduler;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.enums.MediaStatus;
import com.ootd.fitme.domain.mediafile.repository.MediaFileRepository;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DisplayName("MediaCleanupScheduler (미디어 청소기) 통합 테스트")
class MediaCleanupSchedulerTest {

    @Autowired
    private MediaCleanupScheduler mediaCleanupScheduler; // 🌟 이름 변경 적용

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private ImageStorage imageStorage;

    private User testUser;
    private MediaFile activeFile;
    private MediaFile pendingFile1;
    private MediaFile pendingFile2;

    @AfterEach
    void tearDown() {
        mediaFileRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @BeforeEach
    void setUp() {
        testUser = User.create(UUID.randomUUID() + "@fitme.com", "pass123");
        userRepository.save(testUser);

        // 1. 정상적으로 서비스 중인 파일 (ACTIVE)
        activeFile = MediaFile.create("https://cdn.fitme.com/active.jpg", "active.jpg", MediaPurpose.CLOTHES, testUser);

        // 2. 삭제 대기 중인 찌꺼기 파일들 (PENDING_DELETE)
        pendingFile1 = MediaFile.create("https://cdn.fitme.com/pending1.jpg", "p1.jpg", MediaPurpose.CLOTHES, testUser);
        pendingFile1.markAsPendingDelete();

        pendingFile2 = MediaFile.create("https://cdn.fitme.com/pending2.jpg", "p2.jpg", MediaPurpose.CLOTHES, testUser);
        pendingFile2.markAsPendingDelete();

        mediaFileRepository.saveAll(List.of(activeFile, pendingFile1, pendingFile2));
    }

    @Test
    @DisplayName("성공: ACTIVE 파일은 건드리지 않고, PENDING_DELETE 파일만 모두 삭제한다.")
    void executeCleanup_Success() {
        // when
        mediaCleanupScheduler.executeCleanup();

        // then
        // 1. Storage API 호출 검증
        verify(imageStorage, times(1)).delete(pendingFile1.getFileUrl());
        verify(imageStorage, times(1)).delete(pendingFile2.getFileUrl());
        verify(imageStorage, times(0)).delete(activeFile.getFileUrl()); // ACTIVE는 안전해야 함

        // 2. DB 삭제 검증
        List<MediaFile> remainingFiles = mediaFileRepository.findAll();
        assertThat(remainingFiles).hasSize(1);
        assertThat(remainingFiles.get(0).getStatus()).isEqualTo(MediaStatus.ACTIVE);
    }

    @Test
    @DisplayName("부분 실패 복원력: 일부 파일 삭제에 에러가 나도, 나머지 파일은 정상적으로 삭제된다.")
    void executeCleanup_PartialFailure_Resilience() {
        // given
        willThrow(new RuntimeException("Azure Storage Timeout"))
                .given(imageStorage).delete(pendingFile1.getFileUrl());

        // when
        mediaCleanupScheduler.executeCleanup();

        // then
        List<MediaFile> remainingFiles = mediaFileRepository.findAll();

        assertThat(remainingFiles).hasSize(2);

        assertThat(remainingFiles).extracting(MediaFile::getStatus)
                .containsExactlyInAnyOrder(MediaStatus.ACTIVE, MediaStatus.PENDING_DELETE);

    }
}
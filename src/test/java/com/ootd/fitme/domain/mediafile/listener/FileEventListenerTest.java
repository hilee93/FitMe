package com.ootd.fitme.domain.mediafile.listener;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.enums.MediaStatus;
import com.ootd.fitme.domain.mediafile.event.FileDeleteEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate; // 🌟 추가

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("FileEventListener 통합 테스트")
class FileEventListenerTest {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ProfileRepository profileRepository;

    @MockitoBean
    private ImageStorage imageStorage;

    private User testUser;
    private String randomEmail;

    @AfterEach
    void tearDown() {
        mediaFileRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @BeforeEach
    void setUp() {
        randomEmail = UUID.randomUUID() + "@fitme.com";
        testUser = User.create(randomEmail, "pass123");
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("성공: 파일 삭제 성공 시 DB 데이터가 완전히 삭제된다.")
    void handleFileDelete_Success() throws InterruptedException {
        // given
        String fileUrl = "https://cdn.fitme.com/" + UUID.randomUUID() + ".jpg";
        MediaFile mediaFile = MediaFile.create(fileUrl, "test.jpg", MediaPurpose.CLOTHES, testUser);
        mediaFileRepository.save(mediaFile);

        transactionTemplate.execute(status -> {
            publisher.publishEvent(new FileDeleteEvent(fileUrl));
            return null;
        });

        // 비동기 작업 대기
        Thread.sleep(1000);

        // then
        assertThat(mediaFileRepository.findByFileUrl(fileUrl)).isEmpty();
        verify(imageStorage, times(1)).delete(fileUrl);
    }

    @Test
    @DisplayName("복구: 3번 재시도 모두 실패 시 상태가 PENDING_DELETE로 변경된다.")
    void handleFileDelete_RetryAndRecover() throws InterruptedException {
        // given
        String fileUrl = "https://cdn.fitme.com/" + UUID.randomUUID() + ".jpg";
        MediaFile mediaFile = MediaFile.create(fileUrl, "test.jpg", MediaPurpose.CLOTHES, testUser);
        mediaFileRepository.save(mediaFile);

        // 무조건 에러 발생하도록 세팅
        willThrow(new RuntimeException("Storage Error")).given(imageStorage).delete(anyString());

        transactionTemplate.execute(status -> {
            publisher.publishEvent(new FileDeleteEvent(fileUrl));
            return null;
        });

        Thread.sleep(6000);

        // then
        verify(imageStorage, times(3)).delete(fileUrl);

        MediaFile updatedMedia = mediaFileRepository.findByFileUrl(fileUrl).orElseThrow();
        assertThat(updatedMedia.getStatus()).isEqualTo(MediaStatus.PENDING_DELETE);
    }
}
package com.ootd.fitme.domain.mediafile.service;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.enums.MediaStatus;
import com.ootd.fitme.domain.mediafile.exception.MediaFileException;
import com.ootd.fitme.domain.mediafile.repository.MediaFileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
@DisplayName("MediaFileService 통합 테스트")
class MediaFileServiceTest {

    @Autowired
    private MediaFileService mediaFileService;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private ImageStorage imageStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create("test@fitme.com", "pass123");
        userRepository.save(testUser);
    }

    @Nested
    @DisplayName("파일 업로드 및 등록 테스트")
    class UploadAndRegister {

        @Test
        @DisplayName("성공: 실제 DB에 MediaFile 데이터가 생성되어야 한다.")
        void upload_Success() {
            // given
            MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes());
            String expectedUrl = "https://cdn.fitme.com/clothes/uuid-name.jpg";
            given(imageStorage.upload(eq(file), anyString())).willReturn(expectedUrl);

            // when
            String resultUrl = mediaFileService.uploadAndRegister(file, MediaPurpose.CLOTHES, testUser);

            // then
            assertThat(resultUrl).isEqualTo(expectedUrl);

            MediaFile savedMedia = mediaFileRepository.findByFileUrl(expectedUrl).orElseThrow();
            assertThat(savedMedia.getOriginalFileName()).isEqualTo("test.jpg");
            assertThat(savedMedia.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(savedMedia.getStatus()).isEqualTo(MediaStatus.ACTIVE);
        }

        @Test
        @DisplayName("실패: 파일이 비어있으면 INVALID_FILE_REQUEST 예외가 발생한다.")
        void upload_Fail_EmptyFile() {
            // given
            MockMultipartFile emptyFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[0]);

            // when & then
            assertThatThrownBy(() -> mediaFileService.uploadAndRegister(emptyFile, MediaPurpose.CLOTHES, testUser))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_REQUEST);
        }

        @Test
        @DisplayName("실패: 허용되지 않은 Content-Type이면 UNSUPPORTED_FILE_FORMAT 예외가 발생한다.")
        void upload_Fail_InvalidContentType() {
            // given
            MockMultipartFile invalidContentTypeFile = new MockMultipartFile("image", "test.pdf", "application/pdf", "content".getBytes());

            // when & then
            assertThatThrownBy(() -> mediaFileService.uploadAndRegister(invalidContentTypeFile, MediaPurpose.CLOTHES, testUser))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_FILE_FORMAT);
        }

        @Test
        @DisplayName("실패: 허용되지 않은 확장자면 UNSUPPORTED_FILE_FORMAT 예외가 발생한다.")
        void upload_Fail_InvalidExtension() {
            // given
            MockMultipartFile invalidExtensionFile = new MockMultipartFile("image", "test.exe", "image/jpeg", "content".getBytes());

            // when & then
            assertThatThrownBy(() -> mediaFileService.uploadAndRegister(invalidExtensionFile, MediaPurpose.CLOTHES, testUser))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_FILE_FORMAT);
        }
    }

    @Nested
    @DisplayName("파일 삭제 요청 테스트")
    class DeleteMedia {

        @Test
        @DisplayName("성공: 삭제 요청 시 상태가 PENDING_DELETE로 변경되어야 한다.")
        void delete_Success() {
            // given
            String fileUrl = "https://cdn.fitme.com/clothes/delete-me.jpg";
            MediaFile mediaFile = MediaFile.create(fileUrl, "old.jpg", MediaPurpose.CLOTHES, testUser);
            mediaFileRepository.save(mediaFile);

            // when
            mediaFileService.deleteMedia(fileUrl, testUser.getId());

            // then
            MediaFile deletedMedia = mediaFileRepository.findByFileUrl(fileUrl).orElseThrow();
            assertThat(deletedMedia.getStatus()).isEqualTo(MediaStatus.PENDING_DELETE);
        }

        @Test
        @DisplayName("실패: 타인의 파일을 삭제하려고 하면 예외가 발생하고 상태가 변경되지 않는다.")
        void delete_Fail_AccessDenied() {
            // given
            String fileUrl = "https://cdn.fitme.com/clothes/others.jpg";
            MediaFile mediaFile = MediaFile.create(fileUrl, "other.jpg", MediaPurpose.CLOTHES, testUser);
            mediaFileRepository.save(mediaFile);

            UUID strangerId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> mediaFileService.deleteMedia(fileUrl, strangerId))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEDIA_FILE_ACCESS_DENIED);

            MediaFile intactMedia = mediaFileRepository.findByFileUrl(fileUrl).orElseThrow();
            assertThat(intactMedia.getStatus()).isEqualTo(MediaStatus.ACTIVE);
        }
    }
}
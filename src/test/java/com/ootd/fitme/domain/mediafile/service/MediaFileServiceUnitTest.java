package com.ootd.fitme.domain.mediafile.service;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.event.FileDeleteEvent;
import com.ootd.fitme.domain.mediafile.exception.MediaFileException;
import com.ootd.fitme.domain.mediafile.repository.MediaFileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("MediaFileService 단위 테스트")
class MediaFileServiceUnitTest {

    @InjectMocks
    private MediaFileService mediaFileService;

    @Mock
    private ImageStorage imageStorage;

    @Mock
    private MediaFileRepository mediaFileRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("uploadAndRegister 테스트")
    class UploadAndRegister {

        @Test
        @DisplayName("성공: 파일을 업로드하고 메타데이터를 DB에 저장한 뒤 URL을 반환한다.")
        void uploadAndRegister_Success() {
            // given
            MultipartFile mockFile = mock(MultipartFile.class);
            User mockUser = mock(User.class);
            String expectedUrl = "https://cdn.fitme.com/clothes/uuid-image.jpg";

            given(mockFile.getOriginalFilename()).willReturn("test.jpg");
            given(imageStorage.upload(mockFile, "clothes")).willReturn(expectedUrl);

            // when
            String resultUrl = mediaFileService.uploadAndRegister(mockFile, MediaPurpose.CLOTHES, mockUser);

            // then
            assertThat(resultUrl).isEqualTo(expectedUrl);
            then(imageStorage).should(times(1)).upload(mockFile, "clothes");
            then(mediaFileRepository).should(times(1)).save(any(MediaFile.class));
        }
    }

    @Nested
    @DisplayName("deleteMedia 테스트")
    class DeleteMedia {

        private final String fileUrl = "https://cdn.fitme.com/clothes/delete-me.jpg";
        private final UUID loginUserId = UUID.randomUUID();

        @Test
        @DisplayName("성공: 파일이 존재하고 소유자가 일치하면 삭제 이벤트를 발행한다.")
        void deleteMedia_Success() {
            // given
            MediaFile mockMediaFile = mock(MediaFile.class);
            given(mediaFileRepository.findByFileUrl(fileUrl)).willReturn(Optional.of(mockMediaFile));
            given(mockMediaFile.isOwner(loginUserId)).willReturn(true);

            // when
            mediaFileService.deleteMedia(fileUrl, loginUserId);

            // then
            then(eventPublisher).should(times(1)).publishEvent(any(FileDeleteEvent.class));
        }

        @Test
        @DisplayName("실패: 파일이 DB에 존재하지 않으면 MEDIA_FILE_NOT_FOUND 예외가 발생한다.")
        void deleteMedia_FileNotFound() {
            // given
            given(mediaFileRepository.findByFileUrl(fileUrl)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mediaFileService.deleteMedia(fileUrl, loginUserId))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEDIA_FILE_NOT_FOUND);

            then(eventPublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패: 소유자가 일치하지 않으면 MEDIA_FILE_ACCESS_DENIED 예외가 발생한다.")
        void deleteMedia_AccessDenied() {
            // given
            MediaFile mockMediaFile = mock(MediaFile.class);
            given(mediaFileRepository.findByFileUrl(fileUrl)).willReturn(Optional.of(mockMediaFile));
            given(mockMediaFile.isOwner(loginUserId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> mediaFileService.deleteMedia(fileUrl, loginUserId))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEDIA_FILE_ACCESS_DENIED);

            then(eventPublisher).shouldHaveNoInteractions();
        }
    }
}
package com.ootd.fitme.domain.mediafile.service;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

            given(mockFile.isEmpty()).willReturn(false);
            given(mockFile.getContentType()).willReturn("image/jpeg");
            given(mockFile.getOriginalFilename()).willReturn("test.jpg");
            given(imageStorage.upload(mockFile, "clothes")).willReturn(expectedUrl);

            // when
            String resultUrl = mediaFileService.uploadAndRegister(mockFile, MediaPurpose.CLOTHES, mockUser);

            // then
            assertThat(resultUrl).isEqualTo(expectedUrl);
            then(imageStorage).should(times(1)).upload(mockFile, "clothes");
            then(mediaFileRepository).should(times(1)).save(any(MediaFile.class));
        }

        @Test
        @DisplayName("실패: 파일이 비어있으면 INVALID_FILE_REQUEST 예외가 발생한다.")
        void uploadAndRegister_Fail_EmptyFile() {
            // given
            MultipartFile mockFile = mock(MultipartFile.class);
            User mockUser = mock(User.class);

            given(mockFile.isEmpty()).willReturn(true);

            // when & then
            assertThatThrownBy(() -> mediaFileService.uploadAndRegister(mockFile, MediaPurpose.CLOTHES, mockUser))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_REQUEST);

            then(imageStorage).shouldHaveNoInteractions();
            then(mediaFileRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패: 허용되지 않은 Content-Type이면 UNSUPPORTED_FILE_FORMAT 예외가 발생한다.")
        void uploadAndRegister_Fail_InvalidContentType() {
            // given
            MultipartFile mockFile = mock(MultipartFile.class);
            User mockUser = mock(User.class);

            given(mockFile.isEmpty()).willReturn(false);
            given(mockFile.getContentType()).willReturn("application/pdf");
            given(mockFile.getOriginalFilename()).willReturn("test.pdf");

            // when & then
            assertThatThrownBy(() -> mediaFileService.uploadAndRegister(mockFile, MediaPurpose.CLOTHES, mockUser))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_FILE_FORMAT);

            then(imageStorage).shouldHaveNoInteractions();
            then(mediaFileRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패: 허용되지 않은 확장자면 UNSUPPORTED_FILE_FORMAT 예외가 발생한다.")
        void uploadAndRegister_Fail_InvalidExtension() {
            // given
            MultipartFile mockFile = mock(MultipartFile.class);
            User mockUser = mock(User.class);

            given(mockFile.isEmpty()).willReturn(false);
            given(mockFile.getContentType()).willReturn("image/jpeg");
            given(mockFile.getOriginalFilename()).willReturn("test.exe");

            // when & then
            assertThatThrownBy(() -> mediaFileService.uploadAndRegister(mockFile, MediaPurpose.CLOTHES, mockUser))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_FILE_FORMAT);

            then(imageStorage).shouldHaveNoInteractions();
            then(mediaFileRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("deleteMedia 테스트")
    class DeleteMedia {

        private final String fileUrl = "https://cdn.fitme.com/clothes/delete-me.jpg";
        private final UUID loginUserId = UUID.randomUUID();

        @Test
        @DisplayName("성공: 파일이 존재하고 소유자가 일치하면 삭제 대기 상태로 변경한다.")
        void deleteMedia_Success() {
            // given
            MediaFile mockMediaFile = mock(MediaFile.class);
            given(mediaFileRepository.findByFileUrl(fileUrl)).willReturn(Optional.of(mockMediaFile));
            given(mockMediaFile.isOwner(loginUserId)).willReturn(true);

            // when
            mediaFileService.deleteMedia(fileUrl, loginUserId);

            // then
            then(mockMediaFile).should(times(1)).markAsPendingDelete();
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
        }

        @Test
        @DisplayName("실패: 소유자가 일치하지 않으면 MEDIA_FILE_ACCESS_DENIED 예외가 발생한다.")
        void deleteMedia_AccessDenied() {
            // given
            MediaFile mockMediaFile = mock(MediaFile.class);
            given(mediaFileRepository.findByFileUrl(fileUrl)).willReturn(Optional.of(mockMediaFile));
            given(mockMediaFile.isOwner(loginUserId)).willReturn(false);

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(UUID.randomUUID());
            given(mockMediaFile.getUser()).willReturn(mockUser);

            // when & then
            assertThatThrownBy(() -> mediaFileService.deleteMedia(fileUrl, loginUserId))
                    .isInstanceOf(MediaFileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEDIA_FILE_ACCESS_DENIED);

            then(mockMediaFile).should(never()).markAsPendingDelete();
        }
    }
}
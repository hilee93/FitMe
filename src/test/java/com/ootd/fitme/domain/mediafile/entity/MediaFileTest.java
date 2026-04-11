package com.ootd.fitme.domain.mediafile.entity;

import com.ootd.fitme.domain.mediafile.enums.MediaPurpose;
import com.ootd.fitme.domain.mediafile.enums.MediaStatus;
import com.ootd.fitme.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("MediaFile 도메인 단위 테스트")
class MediaFileTest {

    @Mock
    private User mockUser;

    private final String testUrl = "https://cdn.fitme.com/storage/clothes/uuid-image.jpg";
    private final String testFileName = "my_clothes.jpg";
    private final MediaPurpose testPurpose = MediaPurpose.CLOTHES;

    @Test
    @DisplayName("MediaFile 생성 성공: 초기 상태는 ACTIVE여야 한다.")
    void create_Success() {
        // when
        MediaFile mediaFile = MediaFile.create(testUrl, testFileName, testPurpose, mockUser);

        // then
        assertThat(mediaFile.getFileUrl()).isEqualTo(testUrl);
        assertThat(mediaFile.getOriginalFileName()).isEqualTo(testFileName);
        assertThat(mediaFile.getPurpose()).isEqualTo(testPurpose);
        assertThat(mediaFile.getUser()).isEqualTo(mockUser);
        assertThat(mediaFile.getStatus()).isEqualTo(MediaStatus.ACTIVE);
    }

    @Test
    @DisplayName("상태 변경: markAsPendingDelete 호출 시 상태가 PENDING_DELETE로 변경된다.")
    void markAsPendingDelete_Success() {
        // given
        MediaFile mediaFile = MediaFile.create(testUrl, testFileName, testPurpose, mockUser);

        // when
        mediaFile.markAsPendingDelete();

        // then
        assertThat(mediaFile.getStatus()).isEqualTo(MediaStatus.PENDING_DELETE);
    }

    @Nested
    @DisplayName("소유권 확인(isOwner) 테스트")
    class IsOwnerTest {

        @Test
        @DisplayName("성공: 파일의 소유자와 로그인 유저의 ID가 일치하면 true를 반환한다.")
        void isOwner_True() {
            // given
            UUID userId = UUID.randomUUID();
            given(mockUser.getId()).willReturn(userId);
            MediaFile mediaFile = MediaFile.create(testUrl, testFileName, testPurpose, mockUser);

            // when
            boolean result = mediaFile.isOwner(userId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("실패: 파일의 소유자와 로그인 유저의 ID가 일치하지 않으면 false를 반환한다.")
        void isOwner_False() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID strangerId = UUID.randomUUID();
            given(mockUser.getId()).willReturn(ownerId);
            MediaFile mediaFile = MediaFile.create(testUrl, testFileName, testPurpose, mockUser);

            // when
            boolean result = mediaFile.isOwner(strangerId);

            // then
            assertThat(result).isFalse();
        }
    }
}
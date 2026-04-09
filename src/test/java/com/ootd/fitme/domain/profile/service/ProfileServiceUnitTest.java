package com.ootd.fitme.domain.profile.service;

import com.ootd.fitme.domain.profile.dto.request.ProfileUpdateRequest;
import com.ootd.fitme.domain.profile.dto.response.ProfileDto;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.enums.Gender;
import com.ootd.fitme.domain.profile.exception.ProfileException;
import com.ootd.fitme.domain.profile.mapper.ProfileMapper;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.region.service.RegionService;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceUnitTest {
    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private RegionService regionService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Nested
    @DisplayName("updateProfile - 위치 정규화")
    class updateProfileLocationNormalizationTest {
        @Test
        @DisplayName("성공 - location(lat/lon) 입력 시 RegionService로 정규화 후 apply")
        void updateProfile_normalizeLocation_success() {
            UUID userId = UUID.randomUUID();

            User user = mock(User.class);
            Profile profile = mock(Profile.class);

            given(user.getId()).willReturn(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            WeatherAPILocation inputLocation = new WeatherAPILocation(
                    37.5665, 126.9780, null, null, List.of()
            );

            WeatherAPILocation resolvedLocation = new WeatherAPILocation(
                    37.5665, 126.9780, 127, 38, List.of("서울특별시", "중구", "소공동")
            );

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "tester",
                    Gender.OTHER,
                    LocalDate.of(2000, 1, 1),
                    inputLocation,
                    3
            );

            given(regionService.resolveLocation(126.9780, 37.5665)).willReturn(resolvedLocation);

            ProfileDto expected = new ProfileDto(
                    userId,
                    "tester",
                    Gender.OTHER,
                    LocalDate.of(2000, 1, 1),
                    resolvedLocation,
                    3,
                    null
            );

            given(profileMapper.toDto(profile)).willReturn(expected);

            ProfileDto result = profileService.updateProfile(userId, request, null);

            assertThat(result).isEqualTo(expected);
            verify(regionService).resolveLocation(126.9780, 37.5665);

            ArgumentCaptor<ProfileUpdateRequest> requestCaptor
                    = ArgumentCaptor.forClass(ProfileUpdateRequest.class);

            verify(profileMapper).apply(eq(profile), requestCaptor.capture());
            ProfileUpdateRequest applied = requestCaptor.getValue();
            assertThat(applied.location()).isEqualTo(resolvedLocation);
            assertThat(applied.gender()).isEqualTo(Gender.OTHER);
            assertThat(applied.name()).isEqualTo("tester");
            assertThat(applied.birthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
            assertThat(applied.temperatureSensitivity()).isEqualTo(3);
        }

        @Test
        @DisplayName("실패 - location에 위도/경도 누락 시 INVALID_INPUT_VALUE")
        void updateProfile_locationMissingLatLon_fail() {
            UUID userId = UUID.randomUUID();

            User user = mock(User.class);
            Profile profile = mock(Profile.class);

            given(user.getId()).willReturn(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            ProfileUpdateRequest badRequest = new ProfileUpdateRequest(
                    "tester",
                    Gender.FEMALE,
                    LocalDate.of(1998, 5, 3),
                    new WeatherAPILocation(null, 126.9780, null, null, null),
                    4
            );

            assertThatThrownBy(() -> profileService.updateProfile(userId, badRequest, null))
                    .isInstanceOf(ProfileException.class)
                    .satisfies(ex -> {
                        ProfileException pe = (ProfileException) ex;
                        assertThat(pe.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
                    });

            verify(regionService, never()).resolveLocation(anyDouble(), anyDouble());
            verify(profileMapper, never()).apply(any(), any());
        }

        @Test
        @DisplayName("성공 - location이 없으면 기존 방식대로 apply (RegionService 미호출)")
        void updateProfile_withoutLocation_success() {
            UUID userId = UUID.randomUUID();

            User user = mock(User.class);
            Profile profile = mock(Profile.class);

            given(user.getId()).willReturn(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "tester",
                    Gender.MALE,
                    LocalDate.of(1995, 10, 10),
                    null,
                    2
            );

            ProfileDto expected = new ProfileDto(
                    userId,
                    "tester",
                    Gender.MALE,
                    LocalDate.of(1995, 10, 10),
                    null,
                    2,
                    null
            );

            given(profileMapper.toDto(profile)).willReturn(expected);
            ProfileDto result = profileService.updateProfile(userId, request, null);

            assertThat(result).isEqualTo(expected);
            verify(regionService, never()).resolveLocation(anyDouble(), anyDouble());

            ArgumentCaptor<ProfileUpdateRequest> requestCaptor
                    = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
            verify(profileMapper).apply(eq(profile), requestCaptor.capture());
            assertThat(requestCaptor.getValue()).isEqualTo(request);
        }
    }

    @Nested
    @DisplayName("updateProfile - 이미지 파트 처리")
    class updateProfileImageHandlingTest {
        @Test
        @DisplayName("성공 - 파일명이 없는 image 파트는 이미지 업로드로 처리하지 않는다")
        void updateProfile_ignoreNonFileImagePart_success() {
            UUID userId = UUID.randomUUID();

            User user = mock(User.class);
            Profile profile = mock(Profile.class);

            given(user.getId()).willReturn(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "tester",
                    Gender.OTHER,
                    LocalDate.of(2000, 1, 1),
                    null,
                    3
            );

            MultipartFile blankFilenameImagePart = mock(MultipartFile.class);
            given(blankFilenameImagePart.isEmpty()).willReturn(false);
            given(blankFilenameImagePart.getOriginalFilename()).willReturn("    ");

            ProfileDto expected = new ProfileDto(
                    userId,
                    "tester",
                    Gender.OTHER,
                    LocalDate.of(2000, 1, 1),
                    null,
                    3,
                    "/storage/img/profile/existing.png"
            );

            given(profileMapper.toDto(profile)).willReturn(expected);

            ProfileDto result = profileService.updateProfile(userId, request, blankFilenameImagePart);

            assertThat(result).isEqualTo(expected);
            verify(profile, never()).updateProfileImageUrl(anyString());
        }

        @Test
        @DisplayName("성공 - 비어있는 image 파트는 이미지 업로드로 처리하지 않는다")
        void updateProfile_ignoreEmptyImagePart_success() {
            UUID userId = UUID.randomUUID();

            User user = mock(User.class);
            Profile profile = mock(Profile.class);

            given(user.getId()).willReturn(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "tester",
                    Gender.OTHER,
                    LocalDate.of(2000, 1, 1),
                    null,
                    3
            );

            MultipartFile emptyImagePart = mock(MultipartFile.class);
            given(emptyImagePart.isEmpty()).willReturn(true);

            ProfileDto expected = new ProfileDto(
                    userId,
                    "tester",
                    Gender.OTHER,
                    LocalDate.of(2000, 1, 1),
                    null,
                    3,
                    "/storage/img/profile/empty.png"
            );

            given(profileMapper.toDto(profile)).willReturn(expected);

            ProfileDto result = profileService.updateProfile(userId, request, emptyImagePart);

            assertThat(result).isEqualTo(expected);
            verify(profile, never()).updateProfileImageUrl(anyString());
        }
    }
}

package com.ootd.fitme.domain.useractivelocation.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.useractivelocation.entity.UserActiveLocation;
import com.ootd.fitme.domain.useractivelocation.repository.UserActiveLocationRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserActiveLocationServiceUnitTest {
    @Mock
    private UserActiveLocationRepository userActiveLocationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserActiveLocationService userActiveLocationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증 정보가 없으면 upsert를 수행하지 않는다")
    void upsertCurrentUser_noAuth_returns() {
        SecurityContextHolder.clearContext();

        userActiveLocationService.upsertCurrentUser(mock(Region.class));

        verifyNoInteractions(userActiveLocationRepository, userRepository);
    }

    @Test
    @DisplayName("region이 null이면 upsert를 수행하지 않는다")
    void upsertCurrentUser_regionNull_returns() {
        setAuthenticatedUser(UUID.randomUUID());

        userActiveLocationService.upsertCurrentUser(null);

        verifyNoInteractions(userActiveLocationRepository, userRepository);
    }

    @Test
    @DisplayName("기존 활성 위치가 있으면 update 경로를 수행한다")
    void upsertCurrentUser_existing_updates() {
        UUID userId = UUID.randomUUID();
        setAuthenticatedUser(userId);

        Region oldRegion = mock(Region.class);
        Region newRegion = mock(Region.class);
        User user = User.create("u1@fitme.com", "password");

        Instant oldActiveAt = Instant.now().minusSeconds(3600);
        UserActiveLocation existing = UserActiveLocation.create(oldActiveAt, user, oldRegion);

        given(userActiveLocationRepository.findByUserId(userId)).willReturn(Optional.of(existing));

        userActiveLocationService.upsertCurrentUser(newRegion);

        verify(userActiveLocationRepository).findByUserId(userId);
        verify(userActiveLocationRepository, never()).save(any());
        verifyNoInteractions(userRepository);

        assertThat(existing.getRegion()).isEqualTo(newRegion);
        assertThat(existing.getLastActiveAt()).isAfter(oldActiveAt);
    }

    @Test
    @DisplayName("기존 활성 위치가 없으면 새로 생성한다")
    void upsertCurrentUser_notExisting_creates() {
        UUID userId = UUID.randomUUID();
        setAuthenticatedUser(userId);

        Region region = mock(Region.class);
        User user = User.create("u2@fitme.com", "password");

        given(userActiveLocationRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        userActiveLocationService.upsertCurrentUser(region);

        verify(userActiveLocationRepository).findByUserId(userId);
        verify(userRepository).findById(userId);

        ArgumentCaptor<UserActiveLocation> captor = ArgumentCaptor.forClass(UserActiveLocation.class);
        verify(userActiveLocationRepository).save(captor.capture());

        UserActiveLocation saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getRegion()).isEqualTo(region);
        assertThat(saved.getLastActiveAt()).isNotNull();
    }

    @Test
    @DisplayName("기존 활성 위치가 없고 유저도 없으면 USER_NOT_FOUND 예외")
    void upsertCurrentUser_notExisting_userNotFound_throws() {
        UUID userId = UUID.randomUUID();
        setAuthenticatedUser(userId);

        Region region = mock(Region.class);

        given(userActiveLocationRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userActiveLocationService.upsertCurrentUser(region))
                .isInstanceOf(UserException.class)
                .extracting(ex -> ((UserException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("findUserIdsByRegionId는 repository 조회를 위임")
    void findUserIdsByRegionId_delegates() {
        UUID regionId = UUID.randomUUID();
        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        given(userActiveLocationRepository.findUserIdsByRegionId(regionId)).willReturn(userIds);

        List<UUID> result = userActiveLocationService.findUserIdsByRegionId(regionId);

        assertThat(result).containsExactlyElementsOf(userIds);
        verify(userActiveLocationRepository).findUserIdsByRegionId(regionId);
    }

    private void setAuthenticatedUser(UUID userId) {
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);

        given(principal.getUserId()).willReturn(userId);

        Authentication authentication = mock(Authentication.class);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(principal);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}

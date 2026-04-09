package com.ootd.fitme.domain.useractivelocation.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.useractivelocation.entity.UserActiveLocation;
import com.ootd.fitme.domain.useractivelocation.repository.UserActiveLocationRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserActiveLocationService {
    private final UserActiveLocationRepository userActiveLocationRepository;
    private final UserRepository userRepository;

    public void upsertCurrentUser(Region region) {
        UUID userId = currentUserId().orElse(null);
        if (userId == null || region == null) {
            return;
        }

        Instant now = Instant.now();

        userActiveLocationRepository.findByUserId(userId)
                .ifPresentOrElse(existing -> existing.update(now, region),
                        () -> {
                            User user = userRepository.findById(userId)
                                    .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
                            userActiveLocationRepository.save(UserActiveLocation.create(now, user, region));
                });
    }

    @Transactional(readOnly = true)
    public List<UUID> findUserIdsByRegionId(UUID regionId) {
        return userActiveLocationRepository.findUserIdsByRegionId(regionId);
    }

    private Optional<UUID> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal customUserPrincipal)) {
            return Optional.empty();
        }

        return Optional.of(customUserPrincipal.getUserId());
    }
}

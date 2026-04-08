package com.ootd.fitme.domain.user.bootstrap;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.bootstrap.AdminBootstrapProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {
    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isEnabled()) {
            log.info("Admin bootstrap skipped (enable=false)");
            return;
        }

        String email = normalize(properties.getEmail());
        String rawPassword = properties.getPassword();
        String profileName = normalize(properties.getProfileName());

        if (email == null || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException("admin.bootstrap enable=true but email/password is missing");
        }

        if (profileName == null) {
            profileName = "admin";
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User created = User.create(email, passwordEncoder.encode(rawPassword));
                    created.updateRole(Role.ADMIN);
                    return userRepository.save(created);
                });

        if (user.getRole() != Role.ADMIN) {
            user.updateRole(Role.ADMIN);
        }

        if (user.isLocked()) {
            user.updateLocked(false);
        }

        if (properties.isResetPassword() || user.getPassword() == null || user.getPassword().isBlank()) {
            user.updatePassword(passwordEncoder.encode(rawPassword));
        }

        if (profileRepository.findByUserId(user.getId()).isEmpty()) {
            profileRepository.save(Profile.createDefault(profileName, user));
        }

        log.info("Admin bootstrap ensured for email={}", email);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}

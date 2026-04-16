package com.ootd.fitme.global.security.oauth2;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User loadedUser = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = createUserInfo(registrationId, loadedUser.getAttributes());

        String provider = userInfo.provider();
        String providerId = trimToNull(userInfo.providerId());

        if (providerId == null) {
            throw oauth2Error("missing_provider_id", "providerId is required");
        }

        String rawName = trimToNull(userInfo.name());
        String email = resolveEmail(provider, providerId, rawName, trimToNull(userInfo.email()));
        String profileName = (rawName != null) ? rawName : provider + "_" + providerId;

        User user = upsertUser(provider, providerId, email, profileName);

        Map<String, Object> mappedAttributes = new LinkedHashMap<>(loadedUser.getAttributes());
        mappedAttributes.put("userId", user.getId().toString());
        mappedAttributes.put("role", user.getRole().name());
        mappedAttributes.put("email", user.getEmail());

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        Collection<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new DefaultOAuth2User(authorities, mappedAttributes, userNameAttributeName);
    }

    private OAuth2UserInfo createUserInfo(String registrationId, Map<String, Object> attributes) {
        try {
            return OAuth2UserInfoFactory.create(registrationId, attributes);
        } catch (IllegalArgumentException e) {
            throw oauth2Error("unsupported_provider", e.getMessage());
        }
    }

    private User upsertUser(String provider, String providerId, String email, String profileName) {
        Optional<User> existing = userRepository.findByProviderAndProviderId(provider, providerId);
        if (existing.isPresent()) {
            User user = existing.get();
            ensureProfileExists(user, profileName);
            return user;
        }

        User created = userRepository.save(User.createWithOAuth(email, provider, providerId));
        ensureProfileExists(created, profileName);
        return created;
    }

    private void ensureProfileExists(User user, String profileName) {
        if (profileRepository.findByUserId(user.getId()).isEmpty()) {
            profileRepository.save(Profile.createDefault(profileName, user));
        }
    }

    private String resolveEmail(String provider, String providerId,String rawName, String email) {
        if (email == null) {
            return fallbackEmail(provider, providerId, rawName);
        }

        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isEmpty()) {
            return email;
        }

        User existing = existingByEmail.get();
        if (provider.equals(existing.getProvider()) && providerId.equals(existing.getProviderId())) {
            return email;
        }

        return fallbackEmail(provider, providerId, rawName);
    }

    private String fallbackEmail(String provider, String providerId, String rawName) {
        if ("kakao".equals(provider)) {
            String nicknamePart = (rawName == null)
                    ? "kakao"
                    : rawName.replace("@", "").replaceAll("\\s+", "");

            if (nicknamePart.isBlank()) {
                nicknamePart = "kakao";
            }

            return nicknamePart + "_" + providerId + "@kakao.com";
        }
        UUID stable = UUID.nameUUIDFromBytes((provider + ":" + providerId).getBytes(StandardCharsets.UTF_8));
        return provider + "_" + stable + "@oauth.fitme.local";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OAuth2AuthenticationException oauth2Error(String code, String description) {
        OAuth2Error error = new OAuth2Error(code, description, null);
        return new OAuth2AuthenticationException(error, description);
    }
}

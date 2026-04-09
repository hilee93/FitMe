package com.ootd.fitme.domain.profile.controller;

import com.ootd.fitme.domain.profile.dto.request.ProfileUpdateRequest;
import com.ootd.fitme.domain.profile.dto.response.ProfileDto;
import com.ootd.fitme.domain.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("#userId == principal.userId")
    public ResponseEntity<ProfileDto> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestPart("request") ProfileUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request, image));
    }
}

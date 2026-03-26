package com.ootd.fitme.domain.user.controller;

import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.request.UserLockUpdateRequest;
import com.ootd.fitme.domain.user.dto.request.UserRoleUpdateRequest;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserDto response = userService.signUp(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateRole(@PathVariable UUID userId,
                                              @Valid @RequestBody UserRoleUpdateRequest userRoleUpdateRequest) {
        UserDto response = userService.updateRole(userId, userRoleUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateLock(@PathVariable UUID userId,
                                              @Valid @RequestBody UserLockUpdateRequest userLockUpdateRequest) {
        UserDto response = userService.updateLock(userId, userLockUpdateRequest);
        return ResponseEntity.ok(response);
    }


}

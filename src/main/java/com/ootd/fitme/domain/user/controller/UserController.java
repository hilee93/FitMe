package com.ootd.fitme.domain.user.controller;

import com.ootd.fitme.domain.user.dto.request.*;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.dto.response.UserDtoCursorResponse;
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

    @PatchMapping("/{userId}/password")
    @PreAuthorize("#userId == principal.userId")
    public ResponseEntity<Void> changePassword(@PathVariable UUID userId,
                                               @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(userId, changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDtoCursorResponse> getUsers(@Valid UserSearchCondition condition) {
        UserDtoCursorResponse response = userService.getUsers(condition);
        return ResponseEntity.ok(response);
    }

}

package com.ootd.fitme.domain.user.controller;

import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}

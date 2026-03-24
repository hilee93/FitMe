package com.ootd.fitme.domain.user.controller;

import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/sign-in")
    public JwtDto signIn(@Valid @RequestBody SignInRequest signInRequest) {
        return userService.signIn(signInRequest);
    }
}

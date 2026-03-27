package com.ootd.fitme.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(
        @NotBlank
        @Email
        String username,

        @NotBlank
        @Size(max = 100)
        String password
) {
}

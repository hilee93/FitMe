package com.ootd.fitme.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserLockUpdateRequest(
        @NotNull
        Boolean locked
) {
}

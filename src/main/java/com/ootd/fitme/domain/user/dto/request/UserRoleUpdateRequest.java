package com.ootd.fitme.domain.user.dto.request;

import com.ootd.fitme.domain.user.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
        @NotNull
        Role role
) {
}

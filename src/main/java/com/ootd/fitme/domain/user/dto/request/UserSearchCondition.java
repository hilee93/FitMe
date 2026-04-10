package com.ootd.fitme.domain.user.dto.request;

import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.enums.SortDirection;
import com.ootd.fitme.domain.user.enums.UserSortBy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record UserSearchCondition(
        String cursor,
        UUID idAfter,

        @Min(1)
        @Max(100)
        Integer limit,

        UserSortBy sortBy,
        SortDirection sortDirection,
        String emailLike,
        Role roleEqual,
        Boolean locked
) {
}

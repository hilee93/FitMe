package com.ootd.fitme.domain.follow.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FollowSearchCondition(
        String cursor,
        UUID idAfter,
        @NotNull Integer limit,
        String nameLike
) {}

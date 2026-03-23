package com.ootd.fitme.domain.follow.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FollowCreateRequest(

        @NotNull
        UUID followerId,

        @NotNull
        UUID followeeId
) {}

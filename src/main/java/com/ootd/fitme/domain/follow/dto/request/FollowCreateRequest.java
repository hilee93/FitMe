package com.ootd.fitme.domain.follow.dto.request;

import java.util.UUID;

public record FollowCreateRequest(
        UUID followerId,
        UUID followeeId
) {}

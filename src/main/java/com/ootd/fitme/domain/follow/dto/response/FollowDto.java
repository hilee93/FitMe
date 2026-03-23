package com.ootd.fitme.domain.follow.dto.response;

import java.util.UUID;

public record FollowDto(
        UUID id,
        UserSummary followee,
        UserSummary follower
) {}

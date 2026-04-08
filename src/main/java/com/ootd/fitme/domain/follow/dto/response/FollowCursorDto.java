package com.ootd.fitme.domain.follow.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FollowCursorDto(
        UUID id,
        UserSummary followee,
        UserSummary follower,
        Instant createdAt // nextCursor 추출용 API 응답에 포함 안 됨
) {}

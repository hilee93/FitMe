package com.ootd.fitme.domain.follow.mapper;

import com.ootd.fitme.domain.follow.dto.response.FollowCursorDto;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.UserSummary;
import com.ootd.fitme.domain.follow.entity.Follow;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowMapper {

    public static FollowDto toDto(Follow follow, UserSummary follower, UserSummary followee) {
        return new FollowDto(follow.getId(), followee, follower);
    }

    public static FollowDto toDto(FollowCursorDto dto) {
        return new FollowDto(dto.id(), dto.followee(), dto.follower());
    }
}

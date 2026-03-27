package com.ootd.fitme.domain.follow.mapper;

import com.ootd.fitme.domain.follow.dto.response.FollowCursorDto;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.UserSummary;
import com.ootd.fitme.domain.follow.entity.Follow;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowMapper {

    public static FollowDto toDto(Follow follow) {
        // TODO : profile 채울 예정
        UserSummary follower = new UserSummary(follow.getFollowerId(), null, null);

        // TODO : profile 채울 예정
        UserSummary followee = new UserSummary(follow.getFolloweeId(), null, null);

        return new FollowDto(follow.getId(), followee, follower);
    }

    public static FollowDto toDto(FollowCursorDto dto) {
        return new FollowDto(dto.id(), dto.followee(), dto.follower());
    }
}

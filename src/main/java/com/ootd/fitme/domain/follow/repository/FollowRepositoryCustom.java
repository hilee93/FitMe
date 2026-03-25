package com.ootd.fitme.domain.follow.repository;

import com.ootd.fitme.domain.follow.dto.response.FollowCursorDto;


import java.util.List;
import java.util.UUID;

public interface FollowRepositoryCustom {

    // 팔로워 나를 팔로우 하는 사람 목록
    List<FollowCursorDto> findFollowers(UUID followeeId, String cursor, UUID idAfter, int limit, String nameLike);

    // 팔로잉 내가 팔로우 하는 사람 목록
    List<FollowCursorDto> findFollowings(UUID followerId, String cursor, UUID idAfter, int limit, String nameLike);
}

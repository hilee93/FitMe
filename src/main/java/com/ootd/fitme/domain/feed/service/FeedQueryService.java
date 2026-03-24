package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedQueryService {

    public FeedResponseDto getFeed(UUID feedId, UUID userId) {
        return null;
    }

}

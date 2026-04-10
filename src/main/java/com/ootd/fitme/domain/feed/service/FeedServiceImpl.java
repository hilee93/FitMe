package com.ootd.fitme.domain.feed.service;


import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.request.FeedUpdateRequestDto;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.event.FeedCreateEvent;
import com.ootd.fitme.domain.feed.exception.FeedAccessDeniedException;
import com.ootd.fitme.domain.feed.exception.FeedLikeAlreadyExistsException;
import com.ootd.fitme.domain.feed.exception.FeedLikeNotFoundException;
import com.ootd.fitme.domain.feed.exception.FeedNotFoundException;
import com.ootd.fitme.domain.feed.repository.FeedRepository;
import com.ootd.fitme.domain.feedclothes.entity.FeedClothes;
import com.ootd.fitme.domain.feedclothes.repository.FeedClothesRepository;
import com.ootd.fitme.domain.feedlike.entity.FeedLike;
import com.ootd.fitme.domain.feedlike.event.FeedLikedCreateEvent;
import com.ootd.fitme.domain.feedlike.repository.FeedLikeRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final UserRepository userRepository;
    private final ClothesRepository clothesRepository;
    private final FeedClothesRepository feedClothesRepository;
    private final FeedQueryService feedQueryService;
    private final FeedLikeRepository feedLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public FeedCursorResponseDto searchFeeds(FeedSearchCondition feedSearchCondition, UUID userId) {
        return feedQueryService.searchFeeds(feedSearchCondition, userId);
    }

    @Override
    @Transactional
    @PreAuthorize("#feedCreateRequest.authorId == authentication.principal.id") // NOTE: 원래면 로그인 인증된 principal값을 컨트롤러에서 넘겨줘야하지만 프론트엔드 요구 계약상 request를 작성자로 하기떄문에 보안을위해 차선으로 authentication과 비교
    public FeedResponseDto createFeed(FeedCreateRequest feedCreateRequest) {
        WeatherForecast weatherForecast = weatherForecastRepository.findById(feedCreateRequest.weatherId()).orElseThrow();  // TODO: 세부 exception 추후 진행
        User user = userRepository.findById(feedCreateRequest.authorId()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        List<Clothes> clothesList = clothesRepository.findAllById(feedCreateRequest.clothesIds());

        Feed feed = Feed.create(
                feedCreateRequest.content(),
                0,
                0,
                weatherForecast,
                user
        );

        Feed savedFeed = feedRepository.save(feed);

        List<FeedClothes> feedClothes = clothesList.stream()
                .map(clothes -> FeedClothes.create(savedFeed, clothes))
                .toList();

        feedClothesRepository.saveAll(feedClothes);

        eventPublisher.publishEvent(
                new FeedCreateEvent(
                        savedFeed.getId(),
                        user.getId(),
                        savedFeed.getContent(),
                        savedFeed.getCreatedAt()
                )
        );

        // NOTE: 여기까지 순수 create 기능을 위한 동작, 아래는 응답값을 위해 queryService로 별도 처리
        return feedQueryService.getFeed(savedFeed.getId(), feedCreateRequest.authorId());
    }

    @Override
    @Transactional
    public void deleteFeed(UUID feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND));
        // TODO: principal userId와 feed의 authorId 비교 자기자신인지 체크
        feedRepository.delete(feed);
    }

    @Override
    @Transactional
    public FeedResponseDto updateFeed(UUID feedId, UUID userId, FeedUpdateRequestDto feedUpdateRequestDto) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND));

        if (!feed.getUser().getId().equals(userId)) {
            throw new FeedAccessDeniedException(ErrorCode.FEED_ACCESS_DENIED);
        }

        feed.updateContent(feedUpdateRequestDto.content());

        return feedQueryService.getFeed(feed.getId(), userId);
    }

    @Override
    @Transactional
    public void likeFeed(UUID feedId, UUID userId) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND));
        if (feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)) {
            throw new FeedLikeAlreadyExistsException(ErrorCode.FEED_LIKE_ALREADY_EXISTS);
        }
        User user = userRepository.findById(userId).orElseThrow();

        FeedLike feedLike = FeedLike.create(feed, user);

        FeedLike savedFeedLike = feedLikeRepository.save(feedLike);

        feedRepository.increaseLikeCount(feedId);// NOTE: 동시성이슈 해결을 위해 원자적 update 처리

        eventPublisher.publishEvent(
                new FeedLikedCreateEvent(
                        savedFeedLike.getId(),
                        feed.getId(),
                        feed.getUser().getId(),
                        user.getId(),
                        feed.getContent(),
                        feedLike.getCreatedAt()

                )
        );
    }

    @Override
    @Transactional
    public void unlikeFeed(UUID feedId, UUID userId) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND));

        FeedLike feedLike = feedLikeRepository.findByFeedIdAndUserId(feedId, userId).orElseThrow(() -> new FeedLikeNotFoundException(ErrorCode.FEED_LIKE_NOT_FOUND));

        feedLikeRepository.delete(feedLike);
        int updatedCount = feedRepository.decreaseLike(feedId);// NOTE: 동시성이슈 해결을 위해 원자적 update 처리
        if (updatedCount == 0) {
            throw new IllegalStateException("더이상 감소 할 수 없습니다.");
        }
    }


}

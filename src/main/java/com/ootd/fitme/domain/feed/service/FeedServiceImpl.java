package com.ootd.fitme.domain.feed.service;


import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentSearchCondition;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.repository.FeedRepository;
import com.ootd.fitme.domain.feedclothes.entity.FeedClothes;
import com.ootd.fitme.domain.feedclothes.repository.FeedClothesRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    final private FeedRepository feedRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final UserRepository userRepository;
    private final ClothesRepository clothesRepository;
    private final FeedClothesRepository feedClothesRepository;
    private final FeedQueryService feedQueryService;

    @Override
    public FeedCursorResponseDto searchFeeds(FeedSearchCondition feedSearchCondition) {
        return null; // TODO: 검색로직 작성
    }

    @Override
    @Transactional
    public FeedResponseDto createFeed(FeedCreateRequest feedCreateRequest) {
        WeatherForecast weatherForecast = weatherForecastRepository.findById(feedCreateRequest.weatherId()).orElseThrow();  // TODO: 세부 exception 추후 진행
        User user = userRepository.findById(feedCreateRequest.authorId()).orElseThrow(); // TODO: 세부 exception 추후 진행
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
        // NOTE: 여기까지 순수 create 기능을 위한 동작, 아래는 응답값을 위해 queryService로 별도 처리
        return feedQueryService.getFeed(savedFeed.getId(), feedCreateRequest.authorId());
    }

    @Override
    public void deleteFeed(UUID feedId) {

    }

    @Override
    public CommentResponseDto addCommentToFeed(FeedCommentCreateRequest feedCommentCreateRequest) {

        return null;
    }

    @Override
    public CommentCursorResponseDto getFeedComments(FeedCommentSearchCondition feedCommentSearchCondition) {

        return null;
    }

    @Override
    public void likeFeed(UUID feedId) {

    }

    @Override
    public void unlikeFeed(UUID feedId) {

    }


}

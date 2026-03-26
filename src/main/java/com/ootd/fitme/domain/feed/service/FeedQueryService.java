package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.feed.dto.response.*;
import com.ootd.fitme.domain.feed.repository.FeedClothesQueryRepository;
import com.ootd.fitme.domain.feed.repository.FeedLikeQueryRepository;
import com.ootd.fitme.domain.feed.repository.FeedQueryRepository;
import com.ootd.fitme.domain.feed.repository.FeedSelectableValueQueryRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedQueryService {

    private final FeedQueryRepository feedQueryRepository;
    private final ProfileRepository profileRepository;
    private final FeedClothesQueryRepository feedClothesQueryRepository;
    private final FeedSelectableValueQueryRepository feedSelectableValueQueryRepository;
    private final FeedLikeQueryRepository feedLikeQueryRepository;

    public FeedResponseDto getFeed(UUID feedId, UUID userId) {

        FeedDetailFlatRow feedDetailFlatRow = feedQueryRepository.findFeedDetail(feedId).orElseThrow(); // 해당 피드의 대략적인 필드들

        Profile profile = profileRepository.findByUserId(feedDetailFlatRow.authorId()).orElseThrow();

        List<FeedClothesFlatRow> clothesFlatRow = feedClothesQueryRepository.findFeedClothes(feedId);

        List<UUID> attributeDefinitionIds = clothesFlatRow.stream()
                .map(FeedClothesFlatRow::attributeDefinitionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, List<String>> feedSelectableValuesByAttributeIds = feedSelectableValueQueryRepository.findFeedSelectableValuesByAttributeIds(attributeDefinitionIds);

        Map<UUID, List<FeedAttributeSummaryDto>> attributesByClothesId = clothesFlatRow.stream()
                .filter(row -> row.attributeDefinitionId() != null)
                .collect(Collectors.groupingBy(
                        FeedClothesFlatRow::clothesId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                row -> new FeedAttributeSummaryDto(
                                        row.attributeDefinitionId(),
                                        row.attributeDefinitionName(),
                                        feedSelectableValuesByAttributeIds.getOrDefault(row.attributeDefinitionId(), List.of()),
                                        row.attributeValue()
                                ),
                                Collectors.toList()
                        )
                ));

        Map<UUID, FeedClothesSummaryDto> clothesMap = new LinkedHashMap<>();
        for (FeedClothesFlatRow row : clothesFlatRow) {
            clothesMap.putIfAbsent(
                    row.clothesId(),
                    new FeedClothesSummaryDto(
                            row.clothesId(),
                            row.clothesName(),
                            row.imageUrl(),
                            row.clothesType(),
                            attributesByClothesId.getOrDefault(row.clothesId(), List.of())
                    )
            );
        }

        boolean likedByMe = feedLikeQueryRepository.existsLike(feedId, userId);

        return new FeedResponseDto(
                feedDetailFlatRow.feedId(),
                feedDetailFlatRow.createdAt(),
                feedDetailFlatRow.updatedAt(),
                new FeedAuthorSummaryDto(
                        feedDetailFlatRow.authorId(),
                        profile.getName(),
                        profile.getProfileImageUrl()
                ),
                new FeedWeatherSummaryDto(
                        feedDetailFlatRow.weatherId(),
                        feedDetailFlatRow.skyStatus(),
                        new FeedPrecipitationSummaryDto(
                                feedDetailFlatRow.precipitationType(),
                                feedDetailFlatRow.precipitationAmount(),
                                feedDetailFlatRow.precipitationProbability()
                        ),
                        new FeedTemperatureSummaryDto(
                                feedDetailFlatRow.currentTemperature(),
                                feedDetailFlatRow.comparedToDayBefore(),
                                feedDetailFlatRow.temperatureMin(),
                                feedDetailFlatRow.temperatureMax()
                        )
                ),
                new ArrayList<>(clothesMap.values()),
                feedDetailFlatRow.content(),
                feedDetailFlatRow.likeCount(),
                feedDetailFlatRow.commentCount(),
                likedByMe
        );
    }

}

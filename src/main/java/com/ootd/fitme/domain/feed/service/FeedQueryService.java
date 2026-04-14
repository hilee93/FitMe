package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.*;
import com.ootd.fitme.domain.feed.dto.response.elasticsearch.FeedSearchHitRow;
import com.ootd.fitme.domain.feed.repository.*;
import com.ootd.fitme.domain.feed.repository.elasticsearch.FeedSearchQueryRepository;
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
    private final FeedProfileQueryRepository feedProfileQueryRepository;
    private final FeedSearchQueryRepository feedSearchQueryRepository;
    private final FeedWeatherQueryRepositoryImpl feedWeatherQueryRepository;

    public FeedResponseDto getFeed(UUID feedId, UUID userId) {

        FeedDetailFlatRow feedDetailFlatRow = feedQueryRepository.findFeedDetail(feedId).orElseThrow(); // 해당 피드의 대략적인 필드들

        Profile profile = profileRepository.findByUserId(feedDetailFlatRow.authorId()).orElseThrow(); // TODO: FeedProfileQueryRepository로 프로젝션

        List<FeedClothesFlatRow> clothesFlatRow = feedClothesQueryRepository.findFeedClothes(feedId);

        List<UUID> attributeDefinitionIds = clothesFlatRow.stream()
                .map(FeedClothesFlatRow::attributeDefinitionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, List<String>> selectableValuesByAttributeId = feedSelectableValueQueryRepository.findFeedSelectableValuesByAttributeIds(attributeDefinitionIds);

        Map<UUID, List<FeedAttributeSummaryDto>> attributesByClothesId = groupAttributesByClothesId(clothesFlatRow, selectableValuesByAttributeId);

        List<FeedClothesSummaryDto> clothesSummaryDtoList = buildClothesSummaryDtoList(clothesFlatRow, attributesByClothesId);

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
                clothesSummaryDtoList,
                feedDetailFlatRow.content(),
                feedDetailFlatRow.likeCount(),
                feedDetailFlatRow.commentCount(),
                likedByMe
        );
    }


    public FeedCursorResponseDto searchFeeds(FeedSearchCondition condition, UUID userId) {
        // NOTE: 기본 루트 피드 리스트 조회 및 커서구조 조합
//        CursorResult<FeedBaseFlatRow> feedFlatRowCursorResult = feedQueryRepository.findFeedListFlatRows(condition);
        CursorResult<FeedSearchHitRow> feedSearchHitRowCursorResult = feedSearchQueryRepository.searchFeeds(condition);

        List<FeedSearchHitRow> feedFlatRows = feedSearchHitRowCursorResult.content(); // NOTE: 피드 리스트만 추출
        List<UUID> feedIds = feedFlatRows.stream()
                .map(FeedSearchHitRow::feedId)
                .toList();

        List<UUID> authorIds = feedFlatRows.stream()
                .map(FeedSearchHitRow::authorId)
                .distinct()
                .toList();

        List<UUID> weatherIds = feedFlatRows.stream()
                .map(FeedSearchHitRow::weatherId)
                .distinct()
                .toList();

        Map<UUID, FeedAuthorSummaryDto> authorsByUserIds = feedProfileQueryRepository.findAuthorsByUserIds(authorIds);


        Map<UUID, FeedWeatherSummaryDto> weatherSummaryByIds = feedWeatherQueryRepository.findWeatherSummaryByIds(weatherIds);

        // feedIds기반 clothes 조회
        List<FeedListClothesFlatRow> clothesRows = feedClothesQueryRepository.findFeedClothesByFeedIds(feedIds);

        List<UUID> attributeDefinitionIds = clothesRows.stream()
                .map(FeedListClothesFlatRow::attributeDefinitionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // NOTE: 해당속성에 해당하는 옵션값들 모음
        Map<UUID, List<String>> selectableValuesByAttributeIds = feedSelectableValueQueryRepository.findFeedSelectableValuesByAttributeIds(attributeDefinitionIds);

        Map<UUID, List<FeedAttributeSummaryDto>> attributesByClothesId = groupAttributesByClothesId(clothesRows, selectableValuesByAttributeIds);

        Map<UUID, List<FeedClothesSummaryDto>> clothesByFeedId = groupClothesByFeedId(clothesRows, attributesByClothesId);

        // NOTE: LikedByMe 조회
        Set<UUID> likedFeedIds = feedLikeQueryRepository.findLikedByFeedIds(feedIds, userId);

        // NOTE: FeedResponse 리스트 조립
        List<FeedResponseDto> feedResponseDtoList = feedFlatRows.stream()
                .map(feedBaseFlatRow -> new FeedResponseDto(
                        feedBaseFlatRow.feedId(),
                        feedBaseFlatRow.createdAt(),
                        feedBaseFlatRow.updatedAt(),
                        authorsByUserIds.get(feedBaseFlatRow.authorId()),
                        weatherSummaryByIds.get(feedBaseFlatRow.weatherId()),
                        clothesByFeedId.getOrDefault(feedBaseFlatRow.feedId(), List.of()),
                        feedBaseFlatRow.content(),
                        feedBaseFlatRow.likeCount(),
                        feedBaseFlatRow.commentCount(),
                        likedFeedIds.contains(feedBaseFlatRow.feedId())
                ))
                .toList();

        return FeedCursorResponseDto.from(feedSearchHitRowCursorResult, feedResponseDtoList, condition.sortBy(), condition.sortDirection());
    }

    private Map<UUID, List<FeedAttributeSummaryDto>> groupAttributesByClothesId(List<? extends FeedClothesRowView> clothesFlatRow, Map<UUID, List<String>> selectableValuesByAttributeId) {
        return clothesFlatRow.stream()
                .filter(row -> row.attributeDefinitionId() != null)
                .collect(Collectors.groupingBy(
                        FeedClothesRowView::clothesId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                row -> new FeedAttributeSummaryDto(
                                        row.attributeDefinitionId(),
                                        row.attributeDefinitionName(),
                                        selectableValuesByAttributeId.getOrDefault(
                                                row.attributeDefinitionId(), List.of()
                                        ),
                                        row.attributeValue()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private List<FeedClothesSummaryDto> buildClothesSummaryDtoList(
            List<FeedClothesFlatRow> clothesFlatRow,
            Map<UUID, List<FeedAttributeSummaryDto>> attributesByClothesId
    ) {
        Map<UUID, FeedClothesSummaryDto> clothesById = new LinkedHashMap<>(); // NOTE: clothesId -> FeedClothesSummaryDto

        for (FeedClothesFlatRow row : clothesFlatRow) {
            clothesById.putIfAbsent(
                    row.clothesId(),
                    new FeedClothesSummaryDto(
                            row.clothesId(),
                            row.clothesName(),
                            row.imageUrl(),
                            row.clothesType(),
                            attributesByClothesId.getOrDefault(
                                    row.clothesId(), List.of()
                            )
                    )
            );
        }

        return new ArrayList<>(clothesById.values());
    }


    private Map<UUID, List<FeedClothesSummaryDto>> groupClothesByFeedId(
            List<FeedListClothesFlatRow> clothesRows,
            Map<UUID, List<FeedAttributeSummaryDto>> attributesByClothesId
    ) {

        Map<UUID, LinkedHashMap<UUID, FeedClothesSummaryDto>> clothesById = new LinkedHashMap<>(); // NOTE: feedId -> (clothesId -> FeedClothesSummaryDto)

        for (FeedListClothesFlatRow row : clothesRows) {
            clothesById.computeIfAbsent(row.feedId(), feedId -> new LinkedHashMap<>()); // NOTE: 있는지 확인후 없으면 feedId -> (clothesId -> FeedClothesSummaryDto) 구조 추가

            LinkedHashMap<UUID, FeedClothesSummaryDto> clothesMap = clothesById.get(row.feedId()); // NOTE: 특정 feedId의 clothesId -> FeedClothesSummaryDto

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

        return clothesById.entrySet().stream() // NOTE: [(feedId, clothesMap), (feedId, clothesMap), ... ] 객체 리스트 형식으로 변경
                .collect(Collectors.toMap( // NOTE: 다시 재조립하여 Map으로 변경
                        Map.Entry::getKey, // NOTE: feedId key
                        entry -> new ArrayList<>(entry.getValue().values()), // NOTE: feedId -> List<FeedClothesSummaryDto> 구조
                        (existingValue, newValue) -> existingValue,
                        LinkedHashMap::new // NOTE: LinkedHashMap<feedId, List<FeedClothesSummaryDto>> 으로 최종 변경
                ));
    }

}

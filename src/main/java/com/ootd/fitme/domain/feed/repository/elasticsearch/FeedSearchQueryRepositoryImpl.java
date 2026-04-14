package com.ootd.fitme.domain.feed.repository.elasticsearch;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.ootd.fitme.domain.feed.document.FeedDocument;
import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import com.ootd.fitme.domain.feed.dto.response.elasticsearch.FeedSearchHitRow;
import com.ootd.fitme.domain.feed.enums.FeedSortCriteria;
import com.ootd.fitme.domain.feed.enums.SortDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedSearchQueryRepositoryImpl implements FeedSearchQueryRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public CursorResult<FeedSearchHitRow> searchFeeds(FeedSearchCondition condition) {
        // TODO: Operations
        Integer size = condition.limit();
        int fetchSize = size + 1;

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if (condition.keywordLike() != null && !condition.keywordLike().isBlank()) {
            boolBuilder.must(query -> query.match(matchBuilder -> matchBuilder
                            .field("content")
                            .query(condition.keywordLike())
                    )
            );
        }

        if (condition.skyStatusEqual() != null) {
            boolBuilder.filter(query -> query.term(termQuery -> termQuery
                    .field("skyStatus")
                    .value(condition.skyStatusEqual().name())
            ));
        }

        if (condition.precipitationTypeEqual() != null) {
            boolBuilder.filter(query -> query.term(termQuery -> termQuery
                    .field("precipitationType")
                    .value(condition.precipitationTypeEqual().name())
            ));
        }

        if (condition.authorIdEqual() != null) {
            boolBuilder.filter(query -> query.term(termQuery -> termQuery
                            .field("authorId")
                            .value(condition.authorIdEqual().toString())
                    )
            );
        }

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(query -> query.bool(boolBuilder.build()))
                .withMaxResults(fetchSize);

        applySort(queryBuilder, condition.sortBy(), condition.sortDirection());

        if (condition.cursor() != null && condition.idAfter() != null) {
            queryBuilder.withSearchAfter(buildSearchAfter(condition));
        }

        SearchHits<FeedDocument> searchHits = elasticsearchOperations.search(queryBuilder.build(), FeedDocument.class);

        boolean hasNext = searchHits.getSearchHits().size() > size;

        List<FeedSearchHitRow> content = searchHits.getSearchHits().stream()
                .limit(size)
                .map(hit -> toRow(hit.getContent()))
                .toList();

        long total = searchHits.getTotalHits();


        return new CursorResult<>(content, hasNext, total);
    }

    private void applySort(
            NativeQueryBuilder queryBuilder,
            FeedSortCriteria sortBy,
            SortDirection sortDirection
    ) {
        SortOrder order = sortDirection == SortDirection.ASCENDING
                ? SortOrder.Asc
                : SortOrder.Desc;

        switch (sortBy) {
            case CREATED_AT -> {
                queryBuilder.withSort(s -> s.field(f -> f
                        .field("createdAt")
                        .order(order)
                ));
                queryBuilder.withSort(s -> s.field(f -> f
                        .field("id")
                        .order(order)
                ));
            }
            case LIKE_COUNT -> {
                queryBuilder.withSort(s -> s.field(f -> f
                        .field("likeCount")
                        .order(order)
                ));
                queryBuilder.withSort(s -> s.field(f -> f
                        .field("id")
                        .order(order)
                ));
            }
        }
    }

    private List<Object> buildSearchAfter(FeedSearchCondition condition) {
        List<Object> searchAfter = new ArrayList<>();

        switch (condition.sortBy()) {
            case CREATED_AT -> searchAfter.add(condition.cursor());
            case LIKE_COUNT -> searchAfter.add(Integer.parseInt(condition.cursor()));
        }

        searchAfter.add(condition.idAfter().toString());
        return searchAfter;
    }

    private FeedSearchHitRow toRow(FeedDocument doc) {
        return new FeedSearchHitRow(
                doc.getId(),
                doc.getUserId(),
                doc.getWeatherForecastId(),
                doc.getContent(),
                doc.getLikeCount(),
                doc.getCommentCount(),
                doc.getSkyStatus(),
                doc.getPrecipitationType(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }


}

package com.ootd.fitme.domain.feed.repository.elasticsearch;

import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeedDocumentCustomRepositoryImpl implements FeedDocumentCustomRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void updateLikeCount(UUID feedId, int likeCount) {
        Document document = Document.create();
        document.put("likeCount", likeCount);

        UpdateQuery updateQuery = UpdateQuery.builder(feedId.toString())
                .withDocument(document)
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("feeds"));
    }

    @Override
    public void updateCommentCount(UUID feedId, int commentCount) {
        Document document = Document.create();
        document.put("commentCount", commentCount);

        UpdateQuery updateQuery = UpdateQuery.builder(feedId.toString())
                .withDocument(document)
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("feeds"));
    }
}

package com.ootd.fitme.domain.feed.repository.elasticsearch;

import com.ootd.fitme.domain.feed.document.FeedDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.UUID;

public interface FeedDocumentRepository extends ElasticsearchRepository<FeedDocument, UUID>, FeedDocumentCustomRepository {
}

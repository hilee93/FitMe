package com.ootd.fitme.domain.feed.listener;

import com.ootd.fitme.domain.feed.document.FeedDocument;
import com.ootd.fitme.domain.feed.event.FeedCreateEvent;
import com.ootd.fitme.domain.feed.event.FeedDeleteEvent;
import com.ootd.fitme.domain.feed.event.FeedUpdateEvent;
import com.ootd.fitme.domain.feed.repository.elasticsearch.FeedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.repositories.enabled",
        havingValue = "true"
)
public class FeedDocumentIndexingListener {

    private final FeedDocumentRepository feedDocumentRepository;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedCreatedEvent(FeedCreateEvent event){

        FeedDocument feedDocument = FeedDocument.create(
                event.feedId(),
                event.createdAt(),
                event.updatedAt(),
                event.content(),
                event.commentCount(),
                event.likeCount(),
                event.weatherForecastId(),
                event.skyStatus(),
                event.precipitationType(),
                event.userId()
        );
        feedDocumentRepository.save(feedDocument);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedDeletedEvent(FeedDeleteEvent event){
        feedDocumentRepository.deleteById(event.feedId());
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedUpdatedEvent(FeedUpdateEvent event){

        FeedDocument updated = FeedDocument.create(
                event.feedId(),
                event.createdAt(),
                event.updatedAt(),
                event.content(),
                event.commentCount(),
                event.likeCount(), // NOTE: Eventual Consistency 결과적 일관성으로 당장 안맞아도 결국 맞아진다는 뜻으로 검색용 read model이라 이정도는 감수한다.
                event.weatherForecastId(),
                event.skyStatus(),
                event.precipitationType(),
                event.userId()
        );
        feedDocumentRepository.save(updated);
    }


}

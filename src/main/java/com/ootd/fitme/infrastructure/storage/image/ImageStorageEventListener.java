package com.ootd.fitme.infrastructure.storage.image;

import com.ootd.fitme.domain.clothes.dto.ImageDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageStorageEventListener {
    private final ImageStorage imageStorage;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteEvent(ImageDeleteEvent event) {
        try {
            imageStorage.delete(event.imageUrl());
        } catch (Exception e) {
            log.warn("이미지 삭제 실패 (고아 파일 발생): {}", event.imageUrl());
        }
    }
}

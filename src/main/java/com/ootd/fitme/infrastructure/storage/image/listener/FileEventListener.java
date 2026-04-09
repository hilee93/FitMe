package com.ootd.fitme.infrastructure.storage.image.listener;

import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import com.ootd.fitme.infrastructure.storage.image.event.FileDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventListener {

    private final ImageStorage imageStorage;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    public void handleFileDeleteEvent(FileDeleteEvent event) {
        log.info("[FileEventListener] 물리적 파일 삭제 이벤트 수신 - URL: {}", event.fileUrl());
        try {
            imageStorage.delete(event.fileUrl());
        } catch (Exception e) {
            log.error("[FileEventListener] 물리 파일 삭제 실패 (고아 파일 발생 가능성) - URL: {}", event.fileUrl(), e);
        }
    }

    @Recover
    public void recover(Exception e, FileDeleteEvent event) {
        log.error("[Recover] 3회 재시도 모두 실패. 수동 확인 필요 - URL: {}", event.fileUrl(), e);
    }
}
package com.ootd.fitme.domain.mediafile.listener;

import com.ootd.fitme.domain.mediafile.repository.MediaFileRepository;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import com.ootd.fitme.domain.mediafile.event.FileDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventListener {

    private final ImageStorage imageStorage;
    private final MediaFileRepository mediaFileRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFileDeleteEvent(FileDeleteEvent event) {
        imageStorage.delete(event.fileUrl());

        mediaFileRepository.deleteByFileUrl(event.fileUrl());
        log.info("[FileEventListener] 미디어 파일 영구 삭제 완료 - URL: {}", event.fileUrl());
    }

    @Recover
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recover(Exception e, FileDeleteEvent event) {
        mediaFileRepository.findByFileUrl(event.fileUrl())
                .ifPresent(mediaFile -> {
                    mediaFile.markAsPendingDelete();
                    log.error("[Recover] 파일 물리 삭제 실패. 배치 처리를 위해 PENDING_DELETE 상태로 전환 - URL: {}", event.fileUrl(), e);
                });
    }
}
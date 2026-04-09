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

    // 🌟 인프라 기술인 ImageStorage는 여기서만 주입받습니다!
    private final ImageStorage imageStorage;

    /**
     * @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
     * DB 트랜잭션이 '성공적으로 커밋된 직후'에만 실행됩니다.
     * DB 롤백이 일어나면 물리 파일 삭제도 취소되는 우아한 처리입니다.
     * @Async 를 붙여서 메인 스레드 응답 속도에 영향을 주지 않고 백그라운드에서 지웁니다.
     */
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
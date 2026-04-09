package com.ootd.fitme.domain.mediafile.scheduler;

import com.ootd.fitme.domain.mediafile.entity.MediaFile;
import com.ootd.fitme.domain.mediafile.enums.MediaStatus;
import com.ootd.fitme.domain.mediafile.repository.MediaFileRepository;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaCleanupScheduler {

    private final MediaFileRepository mediaFileRepository;
    private final ImageStorage imageStorage;

    // 초 분 시 일 월 요일 -> 매월 1일 새벽 4시 0분 0초에 실행
    @Scheduled(cron = "0 0 4 1 * *")
    public void executeCleanup() {
        log.info("🧹 [MediaCleanupBatch] 매월 1일 미디어 가비지 컬렉션(GC) 시작");

        List<MediaFile> pendingFiles = mediaFileRepository.findByStatus(MediaStatus.PENDING_DELETE);

        if (pendingFiles.isEmpty()) {
            log.info("✅ [MediaCleanupBatch] 삭제할 파일이 없습니다. GC 종료");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (MediaFile file : pendingFiles) {
            try {
                imageStorage.delete(file.getFileUrl());

                mediaFileRepository.delete(file);
                successCount++;

            } catch (Exception e) {
                log.error("❌ [MediaCleanupBatch] 물리 삭제 실패 (다음 달에 재시도) - URL: {}", file.getFileUrl(), e);
                failCount++;
            }
        }

        log.info("🏁 [MediaCleanupBatch] 가비지 컬렉션(GC) 종료. 대상: {}건 | 성공: {}건 | 실패: {}건",
                pendingFiles.size(), successCount, failCount);
    }
}
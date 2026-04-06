package com.ootd.fitme.infrastructure.storage.log;

import com.ootd.fitme.infrastructure.storage.log.LogStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LogUploadScheduler {

    private final LogStorage logStorage;
    private final String logPath;

    public LogUploadScheduler(
            LogStorage logStorage,
            @Value("${fitme.log.path:./.logs}") String logPath
    ) {
        this.logStorage = logStorage;
        this.logPath = logPath;
    }

    @Scheduled(cron = "0 5 * * * *")
    public void uploadPreviousHourLog() {
        LocalDateTime previousHour = LocalDateTime.now().minusHours(1);
        String formattedHour = previousHour.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));

        // 찾아야 할 파일명: 예) app-2026-04-05-13.log
        String targetFileName = "app-" + formattedHour + ".log";
        File targetFile = new File(logPath, targetFileName);

        if (targetFile.exists()) {
            try {
                log.info("[LogUploadScheduler] 로그 백업 시작: {}", targetFileName);
                logStorage.archiveLogFile(targetFile);
                log.info("[LogUploadScheduler] 로그 백업 완료: {}", targetFileName);
            } catch (Exception e) {
                log.error("[LogUploadScheduler] 로그 백업 실패: {}", targetFileName, e);
            }
        } else {
            log.info("[LogUploadScheduler] 백업할 로그 파일이 없습니다: {}", targetFileName);
        }
    }
}
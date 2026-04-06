package com.ootd.fitme.infrastructure.storage.log;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.exception.StorageException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnProperty(name = "fitme.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalLogStorage implements LogStorage {

    @Value("${fitme.storage.local.root-path:./storage}")
    private String rootPath;

    private static final String LOG_BACKUP_FOLDER = "logbackup";

    private Path logBackupDirPath;

    @PostConstruct
    public void init() {
        logBackupDirPath = Paths.get(rootPath, LOG_BACKUP_FOLDER);

        File dir = logBackupDirPath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // 서버가 종료될 때 실행되어 테스트용 로그 데이터 전체 삭제
    @PreDestroy
    public void cleanUp() {
        FileSystemUtils.deleteRecursively(logBackupDirPath.toFile());
    }

    @Override
    public void archiveLogFile(File logFile) {
        if (logFile == null || !logFile.exists()) {
            return;
        }

        String dateDirectory = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Path targetPath = logBackupDirPath.resolve(dateDirectory).resolve(logFile.getName());

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(logFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new StorageException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void sendLogEvent(String logJson) {
        // 로컬 환경에서는 콘솔에 출력하거나 파일에 직접 append 할 수 있습니다.
        // ELK 도입 전 테스트용이므로 비워두어도 무방합니다.
        System.out.println("[LocalLogStorage] Event Received: " + logJson);
    }
}
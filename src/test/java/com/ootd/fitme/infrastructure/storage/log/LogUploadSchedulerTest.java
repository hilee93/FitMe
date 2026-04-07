package com.ootd.fitme.infrastructure.storage.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogUploadSchedulerTest {

    @Mock
    private LogStorage logStorage;

    @InjectMocks
    private LogUploadScheduler logUploadScheduler;

    @TempDir
    Path tempLogDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(logUploadScheduler, "logPath", tempLogDir.toString());
    }

    @Test
    @DisplayName("직전 1시간의 로그 파일이 존재하면 아카이빙 로직을 호출한다")
    void uploadPreviousHourLog_FileExists_CallsArchive() throws IOException {
        // given
        LocalDateTime previousHour = LocalDateTime.now().minusHours(1);
        String formattedHour = previousHour.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String targetFileName = "app-" + formattedHour + ".log";

        Path dummyLogFile = tempLogDir.resolve(targetFileName);
        Files.createFile(dummyLogFile);

        // when
        logUploadScheduler.uploadPreviousHourLog();

        // then
        verify(logStorage, times(1)).archiveLogFile(any(File.class));
    }

    @Test
    @DisplayName("직전 1시간의 로그 파일이 존재하지 않으면 아카이빙 로직을 호출하지 않는다")
    void uploadPreviousHourLog_NoFile_DoesNotCallArchive() {
        // given (파일 생성 안 함)

        // when
        logUploadScheduler.uploadPreviousHourLog();

        // then
        verify(logStorage, never()).archiveLogFile(any(File.class));
    }
}
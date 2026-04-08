package com.ootd.fitme.infrastructure.storage.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class LocalLogStorageTest {

    private LocalLogStorage localLogStorage;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        localLogStorage = new LocalLogStorage();
        ReflectionTestUtils.setField(localLogStorage, "rootPath", tempDir.toString());
        localLogStorage.init();
    }

    @Test
    @DisplayName("로그 파일 아카이빙 시 오늘 날짜 폴더에 파일이 복사된다")
    void archiveLogFile_Success() throws IOException {
        // given
        Path sourceLogPath = tempDir.resolve("test-app.log");
        Files.writeString(sourceLogPath, "log content");
        File sourceFile = sourceLogPath.toFile();

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // when
        localLogStorage.archiveLogFile(sourceFile);

        // then
        Path archivedPath = tempDir.resolve("logbackup").resolve(dateDir).resolve("test-app.log");
        assertThat(Files.exists(archivedPath)).isTrue();
        assertThat(Files.readString(archivedPath)).isEqualTo("log content");
    }
}
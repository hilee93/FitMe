package com.ootd.fitme.infrastructure.storage.log;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AzureLogStorageTest {

    @Mock
    private BlobContainerClient logContainerClient;

    @Mock
    private BlobClient blobClient;

    @InjectMocks
    private AzureLogStorage azureLogStorage;

    @Test
    @DisplayName("로그 아카이빙 시 날짜가 포함된 blobName으로 업로드를 호출한다")
    void archiveLogFile_Success(@TempDir Path tempDir) throws IOException {
        // given
        Path logPath = tempDir.resolve("app-test.log");
        Files.createFile(logPath);
        File logFile = logPath.toFile();

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String expectedBlobName = "log/" + dateDir + "/app-test.log";

        given(logContainerClient.getBlobClient(expectedBlobName)).willReturn(blobClient);

        // when
        azureLogStorage.archiveLogFile(logFile);

        // then
        verify(blobClient).uploadFromFile(eq(logFile.getAbsolutePath()), eq(true));
    }
}
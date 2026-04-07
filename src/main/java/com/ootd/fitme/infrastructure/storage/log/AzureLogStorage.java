package com.ootd.fitme.infrastructure.storage.log;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnProperty(name = "fitme.storage.type", havingValue = "azure")
public class AzureLogStorage implements LogStorage {

    @Value("${fitme.storage.azure.log-credentials.account-name}")
    private String logAccountName;

    @Value("${fitme.storage.azure.log-credentials.account-key}")
    private String logAccountKey;

    @Value("${fitme.storage.azure.blob.log-container}")
    private String logContainerName;

    private BlobContainerClient logContainerClient;

    @PostConstruct
    public void init() {
        if (logAccountName == null || logAccountName.isBlank() || "invalid_key".equals(logAccountName)) {
            throw new StorageException(ErrorCode.INVALID_STORAGE_KEY);
        }

        try {
            StorageSharedKeyCredential credential = new StorageSharedKeyCredential(logAccountName, logAccountKey);
            String endpoint = String.format("https://%s.blob.core.windows.net", logAccountName);

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(credential)
                    .buildClient();

            this.logContainerClient = blobServiceClient.getBlobContainerClient(logContainerName);

            if (!logContainerClient.exists()) {
                logContainerClient.create();
            }
        } catch (Exception e) {
            throw new StorageException(ErrorCode.INVALID_STORAGE_KEY);
        }
    }

    @Override
    public void archiveLogFile(File logFile) {
        if (logFile == null || !logFile.exists()) {
            return;
        }

        String dateDirectory = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String blobName = "log/" + dateDirectory + "/" + logFile.getName();

        try {
            BlobClient blobClient = logContainerClient.getBlobClient(blobName);
            blobClient.uploadFromFile(logFile.getAbsolutePath(), true);
        } catch (Exception e) {
            throw new StorageException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void sendLogEvent(String logJson) {
        //Todo: 추후 ELK 등 별도 파이프라인 연동 시 구현
    }
}
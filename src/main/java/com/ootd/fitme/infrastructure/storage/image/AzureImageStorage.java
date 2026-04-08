package com.ootd.fitme.infrastructure.storage.image;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "fitme.storage.type", havingValue = "azure")
public class AzureImageStorage implements ImageStorage {

    @Value("${fitme.storage.azure.credentials.account-name}")
    private String accountName;

    @Value("${fitme.storage.azure.credentials.account-key}")
    private String accountKey;

    @Value("${fitme.storage.azure.blob.container}")
    private String containerName;

    @Value("${fitme.storage.azure.blob.base-url}")
    private String cdnDomain;

    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        if (accountName == null || accountName.isBlank() || "invalid_key".equals(accountName)) {
            throw new StorageException(ErrorCode.INVALID_STORAGE_KEY);
        }

        try {
            StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
            String endpoint = String.format("https://%s.blob.core.windows.net", accountName);

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(credential)
                    .buildClient();

            this.containerClient = blobServiceClient.getBlobContainerClient(containerName);

            if (!containerClient.exists()) {
                containerClient.create();
            }
        } catch (Exception e) {
            throw new StorageException(ErrorCode.INVALID_STORAGE_KEY);
        }
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String safeDirectory = (directory != null) ? directory.replaceAll("^/+|/+$", "") : "etc";

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String blobName =  "img/" + safeDirectory + "/" + UUID.randomUUID() + extension;

        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            return generateCdnLink(blobName);

        } catch (IOException e) {
            throw new StorageException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String blobName = extractBlobNameFromUrl(fileUrl);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.deleteIfExists();
        } catch (Exception e) {
            throw new StorageException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private String generateCdnLink(String blobName) {
        String baseUrl = cdnDomain.endsWith("/") ? cdnDomain : cdnDomain + "/";
        return baseUrl + containerName + "/" + blobName;
    }

    private String extractBlobNameFromUrl(String fileUrl) {
        String prefix = cdnDomain.endsWith("/") ? cdnDomain + containerName + "/" : cdnDomain + "/" + containerName + "/";
        return fileUrl.replace(prefix, "");
    }
}
package com.ootd.fitme.infrastructure.storage.image;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.exception.StorageException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Slf4j
@ConditionalOnProperty(name = "fitme.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalImageStorage implements ImageStorage {

    @Value("${fitme.storage.local.root-path:./storage}")
    private String localDir;

    @Value("${server.port:8080}")
    private String port;

    private String localDomain;

    @PostConstruct
    public void init() {
        localDomain = "http://localhost:" + port + "/storage/";
        File dir = new File(localDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // 서버가 종료될 때 실행되어 테스트용 이미지 데이터 전체 삭제
    @PreDestroy
    public void cleanUp() {
        FileSystemUtils.deleteRecursively(Paths.get(localDir).toFile());
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

        String relativePath = "img/" + safeDirectory + "/" + UUID.randomUUID() + extension;
        Path targetPath = Paths.get(localDir, relativePath);

        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toAbsolutePath().toFile());

            return localDomain + relativePath;

        } catch (IOException e) {
            log.error("[LocalImageStorage] 파일 저장 중 오류 발생 - targetPath: {}", targetPath.toAbsolutePath(), e);
            throw new StorageException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String relativePath = fileUrl.replace(localDomain, "");
            Path filePath = Paths.get(localDir, relativePath);
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            throw new StorageException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}
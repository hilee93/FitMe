package com.ootd.fitme.infrastructure.storage.image;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
    String upload(MultipartFile file, String directory);
    void delete(String fileUrl);
}
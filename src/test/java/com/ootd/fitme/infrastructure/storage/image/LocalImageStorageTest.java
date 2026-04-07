package com.ootd.fitme.infrastructure.storage.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalImageStorageTest {

    private LocalImageStorage localImageStorage;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        localImageStorage = new LocalImageStorage();
        ReflectionTestUtils.setField(localImageStorage, "localDir", tempDir.toString());
        ReflectionTestUtils.setField(localImageStorage, "port", "8080");
        localImageStorage.init();
    }

    @Test
    @DisplayName("파일 업로드 성공 시 로컬 경로에 파일이 생성되고 URL을 반환한다")
    void upload_Success() {
        // given
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test content".getBytes());

        // when
        String resultUrl = localImageStorage.upload(file, "profile");

        // then
        assertThat(resultUrl).startsWith("http://localhost:8080/storage/img/profile/");
        assertThat(resultUrl).endsWith(".png");

        String relativePath = resultUrl.replace("http://localhost:8080/storage/", "");
        Path savedFile = tempDir.resolve(relativePath);
        assertThat(Files.exists(savedFile)).isTrue();
    }

    @Test
    @DisplayName("파일 삭제 성공 시 로컬 경로에서 파일이 제거된다")
    void delete_Success() {
        // given
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test content".getBytes());
        String uploadedUrl = localImageStorage.upload(file, "profile");

        // when
        localImageStorage.delete(uploadedUrl);

        // then
        String relativePath = uploadedUrl.replace("http://localhost:8080/storage/", "");
        Path savedFile = tempDir.resolve(relativePath);
        assertThat(Files.exists(savedFile)).isFalse();
    }

    @Test
    @DisplayName("비어있는 파일을 업로드하면 null을 반환한다")
    void upload_EmptyFile_ReturnsNull() {
        // given
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        // when
        String resultUrl = localImageStorage.upload(emptyFile, "profile");

        // then
        assertThat(resultUrl).isNull();
    }
}
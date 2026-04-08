package com.ootd.fitme.infrastructure.storage.image;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AzureImageStorageTest {

    @Mock
    private BlobContainerClient containerClient;

    @Mock
    private BlobClient blobClient;

    @InjectMocks
    private AzureImageStorage azureImageStorage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(azureImageStorage, "containerName", "test-container");
        ReflectionTestUtils.setField(azureImageStorage, "cdnDomain", "https://test.azureedge.net");
    }

    @Test
    @DisplayName("정상적인 파일을 업로드하면 Azure Blob에 전송하고 CDN URL을 반환한다")
    void upload_Success() {
        // given
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        given(containerClient.getBlobClient(anyString())).willReturn(blobClient);

        // when
        String resultUrl = azureImageStorage.upload(file, "profile");

        // then
        verify(blobClient).upload(any(InputStream.class), anyLong(), anyBoolean());
        assertThat(resultUrl).startsWith("https://test.azureedge.net/test-container/img/profile/");
        assertThat(resultUrl).endsWith(".jpg");
    }

    @Test
    @DisplayName("파일 삭제 요청 시 정확한 blobName을 추출하여 삭제 메서드를 호출한다")
    void delete_Success() {
        // given
        String targetUrl = "https://test.azureedge.net/test-container/img/clothes/uuid.jpg";
        given(containerClient.getBlobClient("img/clothes/uuid.jpg")).willReturn(blobClient);

        // when
        azureImageStorage.delete(targetUrl);

        // then
        verify(blobClient).deleteIfExists();
    }
}
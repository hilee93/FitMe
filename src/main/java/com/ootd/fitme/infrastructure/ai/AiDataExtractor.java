package com.ootd.fitme.infrastructure.ai;

import com.ootd.fitme.domain.clothes.dto.AiClothesResult;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.ai.exception.AiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiDataExtractor {

    private final ChatClient chatClient;

    public AiDataExtractor(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateText(String promptText, String dataText) {
        log.info("[AiDataExtractor] 텍스트 생성 요청");

        return chatClient.prompt()
                .user(u -> u.text("{promptText}\n\n[분석할 데이터]\n{dataText}")
                        .param("promptText", promptText)
                        .param("dataText", dataText))
                .call()
                .content();
    }

    public <T> T extractData(String rawData, String systemInstruction, Class<T> responseType) {
        BeanOutputConverter<AiClothesResult> converter = new BeanOutputConverter<>(AiClothesResult.class);
        String formatInstructions = converter.getFormat();
        log.info("[AiDataExtractor] 데이터 분석 요청 시작 - 반환 타입: {}, 데이터 길이: {}",
                responseType.getSimpleName(), rawData.length());

        try {

            return chatClient.prompt()
                    .system(systemInstruction)
                    .user(rawData)
                    .call()
                    .entity(responseType);

        } catch (Exception e) {
            log.error("[AiDataExtractor] AI 분석 실패 - 원인: {}", e.getMessage(), e);
            throw new AiException(ErrorCode.ERROR_OCCURRED_DURING_ANALYSIS);
        }
    }
}
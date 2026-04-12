package com.ootd.fitme.infrastructure.ai;

import com.ootd.fitme.domain.clothes.dto.AiClothesResult;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.ai.exception.AiException;
import com.ootd.fitme.infrastructure.scraper.exception.ScraperException; // 적절한 커스텀 예외로 변경하세요
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiDataExtractor {

    private final ChatClient chatClient;

    public AiDataExtractor(ChatClient.Builder chatClientBuilder) {
        // 빌더 단계에서 공통 옵션을 주입할 수도 있습니다.
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * @param rawData 분석할 원본 텍스트
     * @param systemInstruction AI에게 내릴 구체적인 지시사항 (역할 부여 등)
     * @param responseType 반환받고 싶은 DTO 클래스 타입
     * @return T 타입으로 구조화(Structured Output)된 자바 객체
     */
    public <T> T extractData(String rawData, String systemInstruction, Class<T> responseType) {
        BeanOutputConverter<AiClothesResult> converter = new BeanOutputConverter<>(AiClothesResult.class);
        String formatInstructions = converter.getFormat();
        log.info("[AiDataExtractor] 데이터 분석 요청 시작 - 반환 타입: {}, 데이터 길이: {}",
                responseType.getSimpleName(), rawData.length());

        try {
            log.info("========== [테스트용 프롬프트 박제] ==========");
            log.info("System:\n{}", systemInstruction);
            log.info("User:\n{}", rawData);
            log.info("Format (JSON 스키마):\n{}", formatInstructions);
            log.info("=========================================");

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
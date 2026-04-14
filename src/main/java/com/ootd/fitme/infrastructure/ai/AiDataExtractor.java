package com.ootd.fitme.infrastructure.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiDataExtractor {

    private final ChatClient chatClient;

    // Spring Boot 3.x + Spring AI 1.0.0-M6 에서는 ChatClient.Builder가 자동 주입됩니다.
    public AiDataExtractor(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * @param rawData 분석할 원본 텍스트
     * @param prompt AI에게 내릴 구체적인 지시사항
     * @param responseType 반환받고 싶은 DTO 클래스 타입
     * @return T 타입으로 구조화(Structured Output)된 자바 객체
     */

    public String generateText(String promptText, String dataText) {
        log.info("[AiDataExtractor] 텍스트 생성 요청");

        return chatClient.prompt()
                .user(u -> u.text("{promptText}\n\n[분석할 데이터]\n{dataText}")
                        .param("promptText", promptText)
                        .param("dataText", dataText))
                .call()
                .content();
    }

    public <T> T extractData(String rawData, String prompt, Class<T> responseType) {
        String finalPrompt = String.format(
                "%s\n\n[분석할 데이터]\n%s", prompt, rawData
        );

        log.info("[AiDataExtractor] 데이터 분석 요청 (반환 타입: {})", responseType.getSimpleName());

        return chatClient.prompt()
                .user(u -> u.text("{promptText}\n\n[분석할 데이터]\n{dataText}")
                        .param("promptText", prompt)
                        .param("dataText", rawData))
                .call()
                .entity(responseType);
    }
}
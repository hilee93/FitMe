package com.ootd.fitme.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Bean
    public ChatClient.Builder chatClientBuilder() {
        log.info("ChatClient.Builder 빈 생성 시작");

        if (apiKey == null || apiKey.isBlank()) {
            log.error("OPENAI API Key 가 설정되지 않았습니다. (spring.ai.openai.api-key 비어 있음)");
            throw new IllegalStateException("spring.ai.openai.api-key 가 비어 있습니다.");
        } else {
            int lenToShow = Math.min(10, apiKey.length());
            String masked = apiKey.substring(0, lenToShow);
            log.info("OpenAI API Key 앞자리: {}... (length={})", masked, apiKey.length());
        }

        log.info("OpenAI Base URL: {}", baseUrl);

        try {
            OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
            OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi);

            ChatClient.Builder builder = ChatClient.builder(chatModel);
            log.info("ChatClient.Builder 생성 성공");
            return builder;

        } catch (Exception e) {
            log.error("ChatClient.Builder 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 설정 실패", e);
        }
    }
}
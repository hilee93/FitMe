package com.ootd.fitme.infrastructure.ai;

import com.ootd.fitme.infrastructure.ai.exception.AiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiDataExtractorUnitTest {

    // 체이닝되는 메서드들(.prompt().user().call().entity() 등)을 쉽게 모킹하기 위해 RETURNS_DEEP_STUBS 사용
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    private AiDataExtractor aiDataExtractor;

    @BeforeEach
    void setUp() {
        // 빌더가 우리가 만든 mock chatClient를 반환하도록 설정
        when(chatClientBuilder.build()).thenReturn(chatClient);

        // 의존성이 주입된 테스트 대상 객체 생성
        aiDataExtractor = new AiDataExtractor(chatClientBuilder);
    }

    @Test
    @DisplayName("generateText: 프롬프트와 데이터를 기반으로 텍스트를 성공적으로 반환한다.")
    void generateText_Success() {
        // given (준비)
        String expectedResponse = "성공적인 AI 텍스트 응답입니다.";

        // generateText 내부의 메서드 체이닝 구조를 모킹하여 expectedResponse를 반환하도록 설정
        when(chatClient.prompt()
                .user(any(java.util.function.Consumer.class))
                .call()
                .content())
                .thenReturn(expectedResponse);

        // when (실행)
        String result = aiDataExtractor.generateText("테스트 프롬프트", "테스트 데이터");

        // then (검증)
        assertThat(result).isEqualTo(expectedResponse);
    }

    // extractData 메서드 테스트를 위한 임시 DTO 클래스
    static class TestDto {
        private String name;
        private int age;

        public TestDto() {}

        public TestDto(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
        public void setName(String name) { this.name = name; }
        public void setAge(int age) { this.age = age; }
    }

    @Test
    @DisplayName("extractData: AI 응답을 성공적으로 객체로 변환하여 반환한다.")
    void extractData_Success() {
        // given (준비)
        TestDto expectedDto = new TestDto("코딩파트너", 20);

        // .entity(Class)가 호출될 때 expectedDto 객체를 바로 반환하도록 모킹
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .entity(eq(TestDto.class)))
                .thenReturn(expectedDto);

        // when (실행)
        TestDto result = aiDataExtractor.extractData("원본 로그 데이터", "JSON으로 추출해줘", TestDto.class);

        // then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("코딩파트너");
        assertThat(result.getAge()).isEqualTo(20);
    }

    @Test
    @DisplayName("extractData: AI 통신이나 객체 변환 중 오류가 발생하면 AiException 예외가 발생한다.")
    void extractData_ExceptionThrown_ThrowsAiException() {
        // given (준비)
        // .entity() 호출 시 Spring AI 내부에서 예외(예: JSON 파싱 실패 등)가 발생했다고 가정
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .entity(eq(TestDto.class)))
                .thenThrow(new RuntimeException("Spring AI 통신 또는 객체 변환 실패"));

        // when & then (실행 및 검증)
        // try-catch 블록에서 Exception을 잡아 AiException으로 잘 던지는지 확인
        assertThatThrownBy(() -> aiDataExtractor.extractData("원본 데이터", "시스템 지시어", TestDto.class))
                .isInstanceOf(AiException.class);
    }
}
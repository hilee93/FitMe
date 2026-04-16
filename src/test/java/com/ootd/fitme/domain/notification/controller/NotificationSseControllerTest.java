package com.ootd.fitme.domain.notification.controller;

import com.ootd.fitme.domain.notification.service.NotificationSseService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = NotificationSseController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class NotificationSseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationSseService notificationSseService;

    private CustomUserPrincipal createPrincipal(UUID userId) {
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);

        given(principal.getUserId()).willReturn(userId);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .when(principal).getAuthorities();

        return principal;
    }

    @Nested
    @DisplayName("SSE 구독")
    class SubscribeTest {

        @Test
        @DisplayName("실패 - 인증 정보가 없으면 401을 반환한다")
        void subscribe_noAuthentication_return401() throws Exception {
            mockMvc.perform(get("/api/sse")
                            .header("User-Agent", "Mozilla/5.0"))
                    .andExpect(status().isUnauthorized());

            then(notificationSseService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("성공 - lastEventId와 User-Agent를 전달하면 200을 반환한다")
        void subscribe_withLastEventId_return200() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.randomUUID();
            String userAgent = "Mozilla/5.0";

            CustomUserPrincipal principal = createPrincipal(userId);

            given(notificationSseService.subscribe(userId, lastEventId, userAgent))
                    .willReturn(new SseEmitter());

            mockMvc.perform(get("/api/sse")
                            .with(user(principal))
                            .param("lastEventId", lastEventId.toString())
                            .header("User-Agent", userAgent))
                    .andExpect(status().isOk());

            then(notificationSseService).should().subscribe(userId, lastEventId, userAgent);
        }

        @Test
        @DisplayName("성공 - lastEventId가 없어도 요청 가능")
        void subscribe_withoutLastEventId_return200() throws Exception {
            UUID userId = UUID.randomUUID();
            String userAgent = "Mozilla/5.0";

            CustomUserPrincipal principal = createPrincipal(userId);

            given(notificationSseService.subscribe(userId, null, userAgent))
                    .willReturn(new SseEmitter());

            mockMvc.perform(get("/api/sse")
                            .with(user(principal))
                            .header("User-Agent", userAgent))
                    .andExpect(status().isOk());

            then(notificationSseService).should().subscribe(userId, null, userAgent);
        }
    }
}
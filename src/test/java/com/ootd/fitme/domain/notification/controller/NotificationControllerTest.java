package com.ootd.fitme.domain.notification.controller;

import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.dto.response.NotificationPageResponse;
import com.ootd.fitme.domain.notification.service.NotificationService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


@WebMvcTest(
        controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private CustomUserPrincipal createPrincipal(UUID userId) {
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);

        given(principal.getUserId()).willReturn(userId);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .when(principal).getAuthorities();

        return principal;
    }

    @Test
    @DisplayName("실패 - 인증 정보가 없으면 401을 반환한다")
    void getNotifications_noAuthentication_return401() throws Exception {

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .param("cursor", "2026-03-26T09:00:00Z")
                        .param("idAfter", "22222222-2222-2222-2222-222222222222")
                        .param("limit", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공 - 인증 정보가 있고 알림 목록 조회 시 200을 반환한다")
    void getNotifications_request_return200() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = createPrincipal(userId);
        String cursor = "2026-03-26T09:00:00Z";
        String idAfter = "22222222-2222-2222-2222-222222222222";

        given(notificationService.getNotifications(any(NotificationPageRequest.class)))
                .willReturn(new NotificationPageResponse(
                        List.of(), null, null, false, 0L, "createdAt", "DESCENDING"
                ));

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .with(user(principal))
                        .param("cursor", cursor)
                        .param("idAfter", idAfter)
                        .param("limit", "10"))
                .andExpect(status().isOk());

        then(notificationService).should().getNotifications(argThat(request ->
                request.userId().equals(userId)
                        && request.cursor().equals(cursor)
                        && request.idAfter().equals(idAfter)
                        && request.limit() == 10
        ));
    }

    @Test
    @DisplayName("실패 - cursor 형식이 올바르지 않으면 400을 반환한다")
    void getNotifications_invalidCursor_return400() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = createPrincipal(userId);

        mockMvc.perform(get("/api/notifications")
                        .with(user(principal))
                        .param("cursor", "2026-03-26")
                        .param("idAfter", "22222222-2222-2222-2222-222222222222")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest());

        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실패 - idAfter가 UUID 형식이 아니면 400을 반환한다")
    void getNotifications_invalidIdAfter_return400() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = createPrincipal(userId);

        mockMvc.perform(get("/api/notifications")
                        .with(user(principal))
                        .param("cursor", "2026-03-26T09:00:00Z")
                        .param("idAfter", "not-uuid")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest());

        then(notificationService).shouldHaveNoInteractions();
    }

    @DisplayName("실패 - limit가 1 미만이면 400을 반환한다")
    void getNotifications_limitLessThanOne_return400() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = createPrincipal(userId);

        mockMvc.perform(get("/api/notifications")
                        .with(user(principal))
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());

        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실패 - limit가 100 초과이면 400을 반환한다")
    void getNotifications_limitGreaterThanHundred_return400() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = createPrincipal(userId);

        mockMvc.perform(get("/api/notifications")
                        .with(user(principal))
                        .param("limit", "101"))
                .andExpect(status().isBadRequest());

        then(notificationService).shouldHaveNoInteractions();
    }

}

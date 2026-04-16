package com.ootd.fitme.domain.notification.controller;

import com.ootd.fitme.domain.notification.dto.request.NotificationDeleteRequest;
import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.dto.response.NotificationPageResponse;
import com.ootd.fitme.domain.notification.service.NotificationService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


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

    @Nested
    @DisplayName("알림 목록조회")
    class GetNotificationsTest {

    @Test
    @DisplayName("실패 - 인증 정보가 없으면 302을 반환한다")
    void getNotifications_noAuthentication_return302() throws Exception {

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .param("cursor", "2026-03-26T09:00:00Z")
                        .param("idAfter", "22222222-2222-2222-2222-222222222222")
                        .param("limit", "10"))
                .andExpect(status().isFound());
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

    @Test
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

    @Nested
    @DisplayName("알림 삭제")// TODO : CSRF 설정에따라 달라지는데 보통안그럴거니
    class NotificationsDeleteTest {
        @Test
        @DisplayName("성공 - 알림 삭제 요청 시 204 No Content 반환")
        void deleteNotification_success() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();

            CustomUserPrincipal principal = createPrincipal(userId);

            doNothing().when(notificationService)
                    .delete(any(NotificationDeleteRequest.class));

            // when & then
            mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
                          .with(user(principal))
                         .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(notificationService).delete(any(NotificationDeleteRequest.class));
        }

        @Test
        @DisplayName("실패 - 인증 정보 없이 삭제 요청 시 401을 반환한다")
        void deleteNotification_noAuthentication_return401() throws Exception {

            UUID notificationId = UUID.randomUUID();

            mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
                            .with(csrf())) // csrf는 넣어야 401만 검증됨
                    .andExpect(status().isFound());

            verify(notificationService, never()).delete(any());
        }


    }

}

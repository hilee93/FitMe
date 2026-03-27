package com.ootd.fitme.domain.notification.controller;


import com.ootd.fitme.domain.notification.dto.request.NotificationPageQueryRequest;
import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.dto.response.NotificationPageResponse;
import com.ootd.fitme.domain.notification.service.NotificationService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<NotificationPageResponse>  getNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @ModelAttribute NotificationPageQueryRequest query
    ) {

        int limit = (query.limit() == null) ? 20 : query.limit();

        NotificationPageRequest request = new NotificationPageRequest(
                principal.getUserId(),
                query.cursor(),
                query.idAfter(),
                limit
        );

        NotificationPageResponse notifications = notificationService.getNotifications(request);

        return ResponseEntity.ok(notifications);
    }

}

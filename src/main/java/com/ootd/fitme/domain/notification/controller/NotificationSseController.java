package com.ootd.fitme.domain.notification.controller;

import com.ootd.fitme.domain.notification.service.NotificationSseService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class NotificationSseController {

    private final NotificationSseService notificationSseService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserPrincipal principal,
                                @RequestParam(value = "LastEventId", required = false) UUID lastEventId) {
        return notificationSseService.subscribe(principal.getUserId(),lastEventId);
    }
}

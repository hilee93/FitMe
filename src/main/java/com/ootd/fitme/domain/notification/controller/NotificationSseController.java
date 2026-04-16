package com.ootd.fitme.domain.notification.controller;

import com.ootd.fitme.domain.notification.service.NotificationSseService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
@Slf4j
public class NotificationSseController {

    private final NotificationSseService notificationSseService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserPrincipal principal,
                                @RequestParam(value = "lastEventId", required = false) UUID lastEventId,
                                @RequestHeader("User-Agent") String userAgent) {
        log.debug("SSE subscribed lastEventId={}", lastEventId);
        return notificationSseService.subscribe(principal.getUserId(),lastEventId,userAgent);
    }


}

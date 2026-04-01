package com.ootd.fitme.domain.directmessage.controller;

import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.ootd.fitme.domain.directmessage.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DirectMessageWebSocketController {

    private final DirectMessageService directMessageService;

    @MessageMapping("/direct-messages_send")
    public void sendDirectMessage(DirectMessageCreateRequest request) {
        directMessageService.sendDirectMessage(request);
    }
}

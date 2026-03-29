package com.ootd.fitme.domain.directmessage.controller;

import com.ootd.fitme.domain.directmessage.controller.docs.DirectMessageControllerDocs;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import com.ootd.fitme.domain.directmessage.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direct-messages")
public class DirectMessageController implements DirectMessageControllerDocs {

    private final DirectMessageService directMessageService;

    @Override
    @GetMapping
    public ResponseEntity<DirectMessageDtoCursorResponse> getDirectMessages(
            UUID userId, String cursor, UUID idAfter, int limit) {
        // TODO : 구현 예정
        return null;
    }
}

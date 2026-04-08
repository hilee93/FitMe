package com.ootd.fitme.domain.directmessage.controller;

import com.ootd.fitme.domain.directmessage.controller.docs.DirectMessageControllerDocs;
import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageSearchCondition;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import com.ootd.fitme.domain.directmessage.service.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            @RequestParam UUID userId,
            @Valid DirectMessageSearchCondition condition) {
        DirectMessageDtoCursorResponse response = directMessageService.getDirectMessages
                (userId, condition.cursor(), condition.idAfter(), condition.limit());
        return ResponseEntity.status(200).body(response);
    }
}

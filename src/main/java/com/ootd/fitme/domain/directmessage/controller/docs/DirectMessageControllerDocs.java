package com.ootd.fitme.domain.directmessage.controller.docs;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "DirectMessage", description = "DirectMessage API")
public interface DirectMessageControllerDocs {

    ResponseEntity<DirectMessageDtoCursorResponse> getDirectMessages(
            UUID userId, String cursor, UUID idAfter, int limit);
}

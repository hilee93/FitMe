package com.ootd.fitme.domain.directmessage.service;

import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;

import java.util.UUID;

public interface DirectMessageService {

    void sendDirectMessage(DirectMessageCreateRequest request);

    DirectMessageDtoCursorResponse getDirectMessages(UUID userId, String cursor, UUID idAfter, int limit);
}

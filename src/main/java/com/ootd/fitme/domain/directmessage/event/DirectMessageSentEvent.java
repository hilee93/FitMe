package com.ootd.fitme.domain.directmessage.event;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;

public record DirectMessageSentEvent(

        DirectMessageDto directMessageDto
) {}

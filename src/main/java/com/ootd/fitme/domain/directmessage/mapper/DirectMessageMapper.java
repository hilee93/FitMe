package com.ootd.fitme.domain.directmessage.mapper;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.UserSummary;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectMessageMapper {

    public static DirectMessageDto toDto(DirectMessage message, UserSummary sender, UserSummary receiver) {
        return new DirectMessageDto(
                message.getId(),
                message.getCreatedAt(),
                sender,
                receiver,
                message.getContent()
        );
    }
}

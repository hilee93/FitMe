package com.ootd.fitme.domain.directmessage.repository;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;

import java.util.List;
import java.util.UUID;

public interface DirectMessageRepositoryCustom {

    List<DirectMessageDto> findDirectMessages(UUID myId, UUID targetId, String cursor, UUID idAfter, int limit);


}

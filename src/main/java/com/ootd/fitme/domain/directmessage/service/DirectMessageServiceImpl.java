package com.ootd.fitme.domain.directmessage.service;

import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import com.ootd.fitme.domain.directmessage.enums.SortBy;
import com.ootd.fitme.domain.directmessage.enums.SortDirection;
import com.ootd.fitme.domain.directmessage.repository.DirectMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageRepository directMessageRepository;


    @Override
    @Transactional
    public DirectMessageDto sendDirectMessage(DirectMessageCreateRequest request) {
        // TODO : 구현 예정
        return null;
    }

    @Override
    public DirectMessageDtoCursorResponse getDirectMessages(UUID userId, String cursor, UUID idAfter, int limit) {

        List<DirectMessageDto> directMessages =
                directMessageRepository.findDirectMessages(userId, cursor, idAfter, limit);

        boolean hasNext = directMessages.size() > limit;
        List<DirectMessageDto> messages = hasNext ? directMessages.subList(0, limit) : directMessages;

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (hasNext) {
            DirectMessageDto lastItem = messages.get(messages.size() - 1);
            nextCursor = lastItem.createdAt().toString();
            nextIdAfter = lastItem.id();
        }
        // TODO : count 다시 구현하기
        return new DirectMessageDtoCursorResponse(
                messages, nextCursor, nextIdAfter, hasNext, 0, SortBy.createdAt, SortDirection.DESCENDING);
    }
}

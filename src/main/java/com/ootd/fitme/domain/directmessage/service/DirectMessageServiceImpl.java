package com.ootd.fitme.domain.directmessage.service;

import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import com.ootd.fitme.domain.directmessage.dto.response.UserSummary;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import com.ootd.fitme.domain.directmessage.enums.SortBy;
import com.ootd.fitme.domain.directmessage.enums.SortDirection;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.domain.directmessage.event.DirectMessageSentEvent;
import com.ootd.fitme.domain.directmessage.mapper.DirectMessageMapper;
import com.ootd.fitme.domain.directmessage.repository.DirectMessageRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public DirectMessageDto sendDirectMessage(DirectMessageCreateRequest request) {

        DirectMessage directMessage = DirectMessage.create(
                request.senderId(), request.receiverId(), request.content());

        directMessageRepository.save(directMessage);

        UserSummary sender = getUserSummary(request.senderId());
        UserSummary receiver = getUserSummary(request.receiverId());

        DirectMessageDto dto = DirectMessageMapper.toDto(directMessage, sender, receiver);

        // Websocket 브로드캐스트용
        eventPublisher.publishEvent(new DirectMessageSentEvent(dto));

        // 알림용 이벤트 발행
        eventPublisher.publishEvent(new DirectMessageCreateEvent(
                directMessage.getId(),
                directMessage.getReceiverId(),
                directMessage.getSenderId(),
                sender.name(),
                directMessage.getContent(),
                directMessage.getCreatedAt()
        ));

        return dto;
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

        long totalCount = directMessageRepository.countBySenderIdOrReceiverId(userId, userId);

        return new DirectMessageDtoCursorResponse(
                messages, nextCursor, nextIdAfter, hasNext, totalCount, SortBy.createdAt, SortDirection.DESCENDING);
    }

    private UserSummary getUserSummary(UUID userId){
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        return new UserSummary(
                profile.getUser().getId(),
                profile.getName(),
                profile.getProfileImageUrl());
    }
}

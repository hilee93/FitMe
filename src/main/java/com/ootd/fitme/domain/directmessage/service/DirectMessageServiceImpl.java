package com.ootd.fitme.domain.directmessage.service;

import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import com.ootd.fitme.domain.directmessage.dto.response.UserSummary;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import com.ootd.fitme.domain.directmessage.enums.SortBy;
import com.ootd.fitme.domain.directmessage.enums.SortDirection;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.domain.directmessage.exception.DirectMessageSenderMisMatchException;
import com.ootd.fitme.domain.directmessage.mapper.DirectMessageMapper;
import com.ootd.fitme.domain.directmessage.repository.DirectMessageRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.exception.auth.AuthException;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
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
    public void sendDirectMessage(DirectMessageCreateRequest request, UUID authUserId) {

        if (!request.senderId().equals(authUserId)){
            throw new DirectMessageSenderMisMatchException(ErrorCode.DM_SENDER_MISMATCH);
        }

        DirectMessage directMessage = DirectMessage.create(
                request.senderId(), request.receiverId(), request.content());

        directMessageRepository.save(directMessage);

        UserSummary sender = getUserSummary(request.senderId());
        UserSummary receiver = getUserSummary(request.receiverId());

        eventPublisher.publishEvent(new DirectMessageCreateEvent(
                directMessage.getId(),
                directMessage.getReceiverId(),
                directMessage.getSenderId(),
                sender.name(),
                sender.profileImageUrl(),
                receiver.name(),
                receiver.profileImageUrl(),
                directMessage.getContent(),
                directMessage.getCreatedAt()
        ));
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

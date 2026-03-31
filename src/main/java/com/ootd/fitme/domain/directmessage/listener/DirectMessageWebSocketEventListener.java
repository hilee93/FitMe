package com.ootd.fitme.domain.directmessage.listener;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.UserSummary;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DirectMessageWebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDirectMessageSent(DirectMessageCreateEvent event) {

        DirectMessageDto dmData = new DirectMessageDto(
                event.messageId(),
                event.createdAt(),
                new UserSummary(event.senderId(), event.senderName(), event.senderProfileImageUrl()),
                new UserSummary(event.receiverId(), event.receiverName(), event.receiverProfileImageUrl()),
                event.message());

        String dmKey = DirectMessage.createDmKey(
                event.senderId(), event.receiverId());

        messagingTemplate.convertAndSend("/sub/direct-messages_" + dmKey, dmData);
    }
}

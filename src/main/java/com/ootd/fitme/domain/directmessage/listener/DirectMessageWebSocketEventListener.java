package com.ootd.fitme.domain.directmessage.listener;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import com.ootd.fitme.domain.directmessage.event.DirectMessageSentEvent;
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
    public void handleDirectMessageSent(DirectMessageSentEvent event) {

        DirectMessageDto directMessageDto = event.directMessageDto();
        String dmKey = DirectMessage.createDmKey(
                directMessageDto.sender().userId(), directMessageDto.receiver().userId());
        messagingTemplate.convertAndSend("/sub/direct-messages_" + dmKey, directMessageDto);
    }
}

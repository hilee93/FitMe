package com.ootd.fitme.domain.directmessage.listener;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.UserSummary;
import com.ootd.fitme.domain.directmessage.event.DirectMessageSentEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DirectMessageWebSocketEventListenerUnitTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private DirectMessageWebSocketEventListener eventListener;

    @Test
    @DisplayName("성공 - 같은 채널로 메시지가 전송된다")
    void handleDirectMessageSent_success_sendToCorrectChannel() {

        //given
        UUID senderId = UUID.fromString("89a71b30-e73f-415f-b791-e8cb9694e5b6");
        UUID receiverId = UUID.fromString("96671e32-ff27-4215-bf96-d0575abc11f4");

        UserSummary sender = new UserSummary(senderId, "보내는사람", null);
        UserSummary receiver = new UserSummary(receiverId, "받는사람", null);

        DirectMessageDto directMessageDto = new DirectMessageDto(
                UUID.randomUUID(), Instant.now(), sender, receiver, "안녕하세요");

        DirectMessageSentEvent directMessageSentEvent = new DirectMessageSentEvent(directMessageDto);

        //when
        eventListener.handleDirectMessageSent(directMessageSentEvent);

        //then
        String channel = "/sub/direct-messages_89a71b30-e73f-415f-b791-e8cb9694e5b6_96671e32-ff27-4215-bf96-d0575abc11f4";
        then(messagingTemplate).should().convertAndSend(channel, directMessageDto);
    }

}
package com.ootd.fitme.domain.directmessage.listener;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.infrastructure.realtime.websocket.DmRedisPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DirectMessageWebSocketEventListenerUnitTest {

    @Mock
    private DmRedisPublisher dmRedisPublisher;

    @InjectMocks
    private DirectMessageWebSocketEventListener eventListener;

    @Test
    @DisplayName("성공 - 같은 채널로 메시지가 전송된다")
    void handleDirectMessageSent_success_sendToCorrectChannel() {

        //given
        UUID messageId = UUID.randomUUID();
        Instant now = Instant.now();
        UUID senderId = UUID.fromString("89a71b30-e73f-415f-b791-e8cb9694e5b6");
        UUID receiverId = UUID.fromString("96671e32-ff27-4215-bf96-d0575abc11f4");

        DirectMessageCreateEvent event = new DirectMessageCreateEvent(
                messageId, receiverId, senderId,
                "보내는사람", null,
                "받는사람", null,
                "안녕하세요", now);

        //when
        eventListener.handleDirectMessageSent(event);

        //then
        then(dmRedisPublisher).should().publish(any(DirectMessageDto.class));
    }

}
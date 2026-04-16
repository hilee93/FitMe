package com.ootd.fitme.infrastructure.realtime.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DmRedisSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void handleMessage(String message) {
        try {
            DirectMessageDto dmData = objectMapper.readValue(message, DirectMessageDto.class);

            String dmKey = DirectMessage.createDmKey(dmData.sender().userId(), dmData.receiver().userId());

            messagingTemplate.convertAndSend("/sub/direct-messages_" + dmKey, dmData);

            log.info("DM redis 수신 성공. senderId={}, receiverId={}",
                    dmData.sender().userId(), dmData.receiver().userId());
        } catch (JsonProcessingException e) {
            log.error("Dm redis 수신 실패", e);
        }
    }
}

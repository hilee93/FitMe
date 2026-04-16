package com.ootd.fitme.infrastructure.realtime.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DmRedisPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(DirectMessageDto dmData) {
        try {
            String message = objectMapper.writeValueAsString(dmData);
            stringRedisTemplate.convertAndSend("dm:messages", message);
            log.info("DM Redis 발행 성공. senderId={}, receiverId={}",
                    dmData.sender().userId(), dmData.receiver().userId());
        } catch (JsonProcessingException e) {
            log.error("DM Redis 발행 실패. senderId={}, receiverId={}",
                    dmData.sender().userId(), dmData.receiver().userId(), e);
            throw new RuntimeException(e);
        }
    }

}

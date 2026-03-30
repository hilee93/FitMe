package com.ootd.fitme.domain.directmessage.service;

import com.ootd.fitme.domain.directmessage.repository.DirectMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceUnitTest {

    @Mock
    private DirectMessageRepository directMessageRepository;

    @InjectMocks
    private DirectMessageServiceImpl directMessageServiceImpl;

    @Nested
    @DisplayName("DM 목록 조회")
    class GetDirectMessagesTest {

        @Test
        @DisplayName("성공 - getDirectMessage 호출 시 findDirectMessages가 호출된다")
        void getDirectMessages_called_findDirectMessages() {

            //given
            UUID userId = UUID.randomUUID();
            int limit = 5;

            //when
            directMessageServiceImpl.getDirectMessages(userId, null, null, limit);

            //then
            then(directMessageRepository).should().findDirectMessages(eq(userId), any(), any(), eq(limit));
        }
    }
}
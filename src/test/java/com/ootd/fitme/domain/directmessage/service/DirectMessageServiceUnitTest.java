package com.ootd.fitme.domain.directmessage.service;

import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.domain.directmessage.repository.DirectMessageRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceUnitTest {

    @Mock
    private DirectMessageRepository directMessageRepository;

    @Mock
    private ApplicationEventPublisher eventPublish;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private DirectMessageServiceImpl directMessageServiceImpl;

    private UUID senderId;
    private UUID receiverId;
    private DirectMessageCreateRequest request;

    @BeforeEach
    void setUp(){
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        request = new DirectMessageCreateRequest(receiverId, senderId, "안녕하세요");
    }

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

    @Nested
    @DisplayName("DM 전송")
    class SendDirectMessageTest {

        @Test
        @DisplayName("성공 - DB 전송 시 저장과 이벤트 발행이 호출된다")
        void sendDirectMessage_called_saveAndEmitEvent() {

            //given
            Profile senderProfile = mock(Profile.class);
            Profile receiverProfile = mock(Profile.class);
            User senderUser = mock(User.class);
            User receiverUser = mock(User.class);

            given(senderProfile.getUser()).willReturn(senderUser);
            given(profileRepository.findByUserId(senderId)).willReturn(Optional.of(senderProfile));
            given(senderUser.getId()).willReturn(senderId);
            given(senderProfile.getName()).willReturn("보내는 사람");
            given(senderProfile.getProfileImageUrl()).willReturn(null);

            given(receiverProfile.getUser()).willReturn(receiverUser);
            given(profileRepository.findByUserId(receiverId)).willReturn(Optional.of(receiverProfile));
            given(receiverUser.getId()).willReturn(receiverId);
            given(receiverProfile.getName()).willReturn("받는 사람");
            given(receiverProfile.getProfileImageUrl()).willReturn(null);

            //when
            DirectMessageDto directMessageDto = directMessageServiceImpl.sendDirectMessage(request);

            //then
            then(directMessageRepository).should().save(any(DirectMessage.class));
            then(eventPublish).should().publishEvent(any(DirectMessageCreateEvent.class));
            assertThat(directMessageDto).isNotNull();
        }

        @Test
        @DisplayName("실패 - sender 프로필이 없으면 예외가 발생한다")
        void sendDirectMessage_senderProfileNotExist_throwException() {

            //given
            given(profileRepository.findByUserId(senderId)).willReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> directMessageServiceImpl.sendDirectMessage(request))
                    .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("실패 - receiver 프로필이 없으면 예외가 발생한다")
        void sendDirectMessage_receiverProfileNotExist_throwException() {

            //given
            Profile senderProfile = mock(Profile.class);
            User senderUser = mock(User.class);
            given(senderProfile.getUser()).willReturn(senderUser);
            given(senderUser.getId()).willReturn(senderId);
            given(profileRepository.findByUserId(senderId)).willReturn(Optional.of(senderProfile));
            given(profileRepository.findByUserId(receiverId)).willReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> directMessageServiceImpl.sendDirectMessage(request))
                    .isInstanceOf(UserException.class);
        }

    }
}
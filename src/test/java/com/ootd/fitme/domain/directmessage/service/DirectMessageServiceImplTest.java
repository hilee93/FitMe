package com.ootd.fitme.domain.directmessage.service;

import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import com.ootd.fitme.domain.directmessage.repository.DirectMessageRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class DirectMessageServiceImplTest {

    @Autowired
    private DirectMessageServiceImpl directMessageServiceImpl;

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private UUID senderId;
    private UUID receiverId;

    @BeforeEach
    void setup() {
        User sender = userRepository.save(User.create("sender@test.com", "123456"));
        User receiver = userRepository.save(User.create("receiver@test.com", "123456"));
        senderId = sender.getId();
        receiverId = receiver.getId();

        saveProfile(sender, "보내는사람");
        saveProfile(receiver, "받는사람");
    }

    @Nested
    @DisplayName("DM 목록 조회")
    class GetDirectMessagesTest {

        @Test
        @DisplayName("성공 - sender로 조회하면 DM이 반환된다")
        void getDirectMessages_sender_returnDirectMessages() {

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "하이"));

            //when
            DirectMessageDtoCursorResponse directMessages = directMessageServiceImpl.getDirectMessages(
                    senderId, null, null, 10);

            //then
            assertThat(directMessages).isNotNull();
            assertThat(directMessages.data().size()).isEqualTo(1);
            assertThat(directMessages.data().get(0).sender().userId()).isEqualTo(senderId);
        }

        @Test
        @DisplayName("성공 - receiver로 조회하면 DM이 반환된다")
        void getDirectMessages_receiver_returnDirectMessages() {

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "잘가"));

            //when
            DirectMessageDtoCursorResponse directMessages = directMessageServiceImpl.getDirectMessages(
                    receiverId, null, null, 10);

            //then
            assertThat(directMessages).isNotNull();
            assertThat(directMessages.data().size()).isEqualTo(1);
            assertThat(directMessages.data().get(0).receiver().userId()).isEqualTo(receiverId);
        }

        @Test
        @DisplayName("성공 - 데이터가 limit보다 많으면 hasNext가 true이다")
        void getDirectMessages_sizeOverLimit_hasNextTrue() {

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트1"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트2"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트3"));

            //when
            DirectMessageDtoCursorResponse directMessages = directMessageServiceImpl.getDirectMessages(
                    senderId, null, null, 2);

            //then
            assertThat(directMessages.hasNext()).isTrue();
            assertThat(directMessages.data().size()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 데이터가 limit보다 적으면 hasNext가 false이다")
        void getDirectMessages_sizeUnderLimit_hasNextFalse() {

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트1"));

            //when
            DirectMessageDtoCursorResponse directMessages = directMessageServiceImpl.getDirectMessages(
                    senderId, null, null, 2);

            //then
            assertThat(directMessages.hasNext()).isFalse();
            assertThat(directMessages.data().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 데이터가 0개면 빈 리스트를 반환한다")
        void getDirectMessages_dataEmpty_returnList() {

            //when
            DirectMessageDtoCursorResponse directMessages = directMessageServiceImpl.getDirectMessages(
                    senderId, null, null, 2);

            //then
            assertThat(directMessages.data()).isEmpty();
            assertThat(directMessages.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - hasNext가 true이면 nextCursor가 null이 아니다")
        void getDirectMessages_hasNextTrue_nextCursorNotNull() {

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트1"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트2"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트3"));

            //when
            DirectMessageDtoCursorResponse directMessages = directMessageServiceImpl.getDirectMessages(
                    senderId, null, null, 2);

            //then
            assertThat(directMessages.hasNext()).isTrue();
            assertThat(directMessages.nextCursor()).isNotNull();
            assertThat(directMessages.nextIdAfter()).isNotNull();
        }

        @Test
        @DisplayName("성공 - hasNext가 false이면 nextCursor가 null이다")
        void getDirectMessages_hasNextFalse_nextCursorNull() {

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트1"));

            //when
            DirectMessageDtoCursorResponse directMessages = directMessageServiceImpl.getDirectMessages(
                    senderId, null, null, 2);

            //then
            assertThat(directMessages.hasNext()).isFalse();
            assertThat(directMessages.nextCursor()).isNull();
            assertThat(directMessages.nextIdAfter()).isNull();
        }

        @Test
        @DisplayName("성공 - totalCount는 전체 DM 개수를 반환한다")
        void getDirectMessages_totalCount_returnTotalCount() {

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트1"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트2"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "DM 테스트3"));

            //when
            DirectMessageDtoCursorResponse directMessages = directMessageServiceImpl.getDirectMessages(
                    senderId, null, null, 2);

            //then
            assertThat(directMessages.totalCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("DM 전송")
    class SendDirectMessageTest {

        @Test
        @DisplayName("성공 - DM이 DB에 저장된다")
        void sendDirectMessage_success_saveDirectMessageDB() {

            //given
            User sender = userRepository.findById(senderId).orElseThrow();
            CustomUserPrincipal userPrincipal = CustomUserPrincipal.from(sender);
            UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(userPrincipal,
                    null, userPrincipal.getAuthorities());

            DirectMessageCreateRequest request = new DirectMessageCreateRequest(
                    receiverId, senderId, "안녕하세요");
            DirectMessageCreateRequest request2 = new DirectMessageCreateRequest(
                    receiverId, senderId, "안녕하세요2");

            //when
            directMessageServiceImpl.sendDirectMessage(request, principal);
            directMessageServiceImpl.sendDirectMessage(request2, principal);

            //then
            List<DirectMessage> result = directMessageRepository.findAll();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContent()).isEqualTo("안녕하세요");
            assertThat(result.get(0).getSenderId()).isEqualTo(senderId);
        }
    }

    private void saveProfile(User user, String name) {
        profileRepository.save(Profile.create(
                name, null, null, 0, 0,
                null, null, null,
                null, null, null,
                user));
    }

}

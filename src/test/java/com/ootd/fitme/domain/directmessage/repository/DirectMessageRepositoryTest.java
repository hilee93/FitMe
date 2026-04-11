package com.ootd.fitme.domain.directmessage.repository;


import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class DirectMessageRepositoryTest {

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Nested
    @DisplayName("DM 목록 조회")
    class GetDirectMessagesTest {

        UUID senderId;
        UUID receiverId;

        @BeforeEach
        void setup() {
            User sender = userRepository.save(User.create("sender@a.com", "123456"));
            User receiver = userRepository.save(User.create("receiver@a.com", "123456"));
            senderId = sender.getId();
            receiverId = receiver.getId();
            saveProfile(sender, "sender");
            saveProfile(receiver, "receiver");
        }

        @Test
        @DisplayName("limit + 1개를 조회한다")
        void findDirectMessages_return_limitPlusOne(){

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "테스트 메시지1"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "테스트 메시지2"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "테스트 메시지3"));

            //when
            List<DirectMessageDto> directMessages = directMessageRepository.findDirectMessages(
                    senderId, receiverId, null,null, 2);

            //then
            assertThat(directMessages.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("cursor로 다음 페이지를 조회한다")
        void findDirectMessages_cursor_returnNextPage() {

            //given
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "테스트 메시지1"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "테스트 메시지2"));
            directMessageRepository.save(DirectMessage.create(senderId, receiverId, "테스트 메시지3"));

            List<DirectMessageDto> firstPage = directMessageRepository.findDirectMessages(
                    senderId,  receiverId,null, null, 2);

            String cursor = firstPage.get(1).createdAt().toString();
            UUID idAfter = firstPage.get(1).id();

            //when
            List<DirectMessageDto> secondPage = directMessageRepository.findDirectMessages(
                    senderId, receiverId, cursor, idAfter, 2);

            //then
            assertThat(secondPage.size()).isEqualTo(1);
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
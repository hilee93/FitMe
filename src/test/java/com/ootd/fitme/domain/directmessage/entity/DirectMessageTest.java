package com.ootd.fitme.domain.directmessage.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DirectMessageTest {

    @Test
    @DisplayName("성공 - 두 UUID는 오름차순으로 정렬되어 반환한다")
    void createDmKey_smallerIdFirst_returnSmallerId() {

        //given
        UUID userId1 = UUID.fromString("89a71b30-e73f-415f-b791-e8cb9694e5b6");
        UUID userId2 = UUID.fromString("96671e32-ff27-4215-bf96-d0575abc11f4");

        //when
        String dmKey = DirectMessage.createDmKey(userId1, userId2);

        //then
        assertThat(dmKey).isEqualTo(
                "89a71b30-e73f-415f-b791-e8cb9694e5b6_96671e32-ff27-4215-bf96-d0575abc11f4");

    }

    @Test
    @DisplayName("성공 - 큰 UUID가 앞에 와도 오름차순으로 정렬되어 반환된다")
    void createDmKey_reverseOrder_returnSmallerId() {

        //given
        UUID userId1 = UUID.fromString("89a71b30-e73f-415f-b791-e8cb9694e5b6");
        UUID userId2 = UUID.fromString("96671e32-ff27-4215-bf96-d0575abc11f4");

        //when
        String dmKey = DirectMessage.createDmKey(userId2, userId1);

        //then
        assertThat(dmKey).isEqualTo(
                "89a71b30-e73f-415f-b791-e8cb9694e5b6_96671e32-ff27-4215-bf96-d0575abc11f4");

    }


    @Test
    @DisplayName("성공 - 두 사용자의 순서가 바뀌어도 같은 key를 반환한다")
    void createDmKey_orderChange_returnSameKey() {

        //given
        UUID userId1 = UUID.fromString("89a71b30-e73f-415f-b791-e8cb9694e5b6");
        UUID userId2 = UUID.fromString("96671e32-ff27-4215-bf96-d0575abc11f4");

        //when
        String dmKey1 = DirectMessage.createDmKey(userId1, userId2);
        String dmKey2 = DirectMessage.createDmKey(userId2, userId1);

        //then
        assertThat(dmKey1).isEqualTo(dmKey2);
    }
}
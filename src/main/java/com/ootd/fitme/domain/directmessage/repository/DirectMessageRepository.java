package com.ootd.fitme.domain.directmessage.repository;

import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID>, DirectMessageRepositoryCustom {

    @Query("SELECT COUNT(dm) FROM DirectMessage dm " +
            "WHERE (dm.senderId = :myId AND dm.receiverId = :targetId) " +
            "OR (dm.senderId = :targetId AND dm.receiverId = :myId)")
    long countByConversation(@Param("myId") UUID myId, @Param("targetId") UUID targetId);
}

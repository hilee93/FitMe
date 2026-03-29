package com.ootd.fitme.domain.directmessage.repository;

import com.ootd.fitme.domain.directmessage.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {


}

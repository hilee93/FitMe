package com.ootd.fitme.domain.notification.repository;

import com.ootd.fitme.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.nio.file.LinkOption;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> , NotificationRepositoryCustom{

    Long countByUserId(UUID userId);

    boolean existsByIdAndUserId(UUID notificationId, UUID userId);
}

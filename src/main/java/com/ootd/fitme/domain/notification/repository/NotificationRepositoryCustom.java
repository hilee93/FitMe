package com.ootd.fitme.domain.notification.repository;

import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.entity.Notification;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {

    Slice<Notification> search(NotificationPageRequest request);
}

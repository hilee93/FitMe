package com.ootd.fitme.domain.notification.mapper;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.entity.Notification;

public class NotificationMapper {
    //순수 메퍼목적 인스턴스 방지
    private NotificationMapper(){}

    public static NotificationDto toDto(Notification notification) {

        if (notification == null) {
            return null;
        }

        return new NotificationDto(
                notification.getId(),
                notification.getCreatedAt(),
                notification.getUser().getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getLevel()
        );
    }
}

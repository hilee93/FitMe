package com.ootd.fitme.domain.notification.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class NotificationBadRequestException extends NotificationException {
    public NotificationBadRequestException(UUID notificationID, UUID userId) {
        super(ErrorCode.NOTIFICATION_BAD_REQUEST,
                Map.of(
                        "notificationId", notificationID,
                        "userId", userId
                )
        );
    }
}
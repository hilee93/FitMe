package com.ootd.fitme.domain.notification.dto.request;

import com.ootd.fitme.domain.notification.exception.NotificationException;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;

import java.util.UUID;

public record NotificationDeleteRequest(
        UUID userId,
        UUID notificationId
) {
    public static NotificationDeleteRequest from(
            CustomUserPrincipal principal,
            UUID notificationId
    ) {

        return new NotificationDeleteRequest(
                principal.getUserId(),
                notificationId
        );
    }
}

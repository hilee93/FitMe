package com.ootd.fitme.domain.notification.entity;

import com.ootd.fitme.domain.base.BaseEntity;
import com.ootd.fitme.domain.notification.enums.NotificationLevel;
import com.ootd.fitme.domain.notification.enums.NotificationType;
import com.ootd.fitme.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(name = "level", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationLevel level;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    private Notification(
            NotificationLevel level,
            String title,
            String content,
            NotificationType type,
            User user
    ) {
        this.level = level;
        this.title = title;
        this.content = content;
        this.type = type;
        this.user = user;
    }

    public static Notification create(
            NotificationLevel level,
            String title,
            String content,
            NotificationType type,
            User user
    ) {
        return new Notification(level, title, content, type, user);
    }


}

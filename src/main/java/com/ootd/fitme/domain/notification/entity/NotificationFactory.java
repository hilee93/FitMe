package com.ootd.fitme.domain.notification.entity;

import com.ootd.fitme.domain.notification.enums.NotificationLevel;
import com.ootd.fitme.domain.notification.enums.NotificationType;
import com.ootd.fitme.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class NotificationFactory {

    public Notification dm(User user, String senderName, String message) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+senderName+"]"+ "님이 메시지를 보냈습니다.",
                message,
                NotificationType.DM,
                user
        );
    }

    public Notification followed(User user, String followerName) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+followerName+"]"+ "님이 회원님을 팔로우했습니다.",
                "팔로워 리스트를 확인하세요.",
                NotificationType.FOLLOWED,
                user
        );
    }
    // TODO : 추후에 weatherAlert가 enum이 생기면 바꿀것
    public Notification weatherAlert(User user,String region_1,String region_2 ,String weatherAlert ) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+weatherAlert +"]"+ ".",
                "",
                NotificationType.WEATHER_ALERT,
                user
        );
    }

    public Notification feedLiked(User user, String likerName) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+likerName +"]"+ "님이 회원님의 피드를 좋아했습니다.",
                "피드 좋아요",
                NotificationType.FEED_LIKED,
                user
        );
    }

    public Notification feedCommented(User user, String commenterName, String comment) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+commenterName+"]"+ "님이 댓글을 남겼습니다: ",
                comment,
                NotificationType.FEED_COMMENTED,
                user
        );
    }

    public Notification followerNewFeed(User user, String followerName, String feedName) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+followerName+"]"+ "님이 새로운 피드를 등록했습니다.",
                feedName,
                NotificationType.FOLLOWER_NEW_FEED,
                user
        );
    }

    public Notification attributeUpdated(User user, String attributeName) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+attributeName+"]"+ " 속성이 변경되었습니다.",
                "속성을 확인해보세요",
                NotificationType.ATTRIBUTE_UPDATED,
                user
        );
    }

    public Notification attributeAdded(User user, String attributeName) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+attributeName+"]"+ " 속성이 추가되었습니다.",
                "속성을 확인해보세요",
                NotificationType.ATTRIBUTE_ADDED,
                user
        );
    }

    public Notification attributeDeleted(User user, String attributeName) {
        return Notification.create(
                NotificationLevel.WARN,
                "["+attributeName+"]"+ " 속성이 삭제되었습니다.",
                "속성을 확인해보세요",
                NotificationType.ATTRIBUTE_DELETED,
                user
        );
    }

}
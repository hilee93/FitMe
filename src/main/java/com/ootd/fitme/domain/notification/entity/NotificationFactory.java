package com.ootd.fitme.domain.notification.entity;

import com.ootd.fitme.domain.notification.enums.AttributeAction;
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
                "["+region_1 +" "+ region_2 +"]"+ "의 날씨가 ["+weatherAlert+"]"+"로 바뀌었습니다",
                "날씨를 확인해 보세요",
                NotificationType.WEATHER_ALERT,
                user
        );
    }

    public Notification feedLiked(User user,String content, String likerName) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+likerName +"]"+ "님이 회원님의 피드에 좋아요를 눌렀습니다.",
                "피드: "+content,
                NotificationType.FEED_LIKED,
                user
        );
    }

    public Notification feedCommented(User user, String commenterName, String comment) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+commenterName+"]"+ "님이 댓글을 남겼습니다.",  // NOTE: 프로토타입 형식으로 변경 제원님 추후 체크 by 태언
                comment,
                NotificationType.FEED_COMMENTED,
                user
        );
    }

    public Notification followerNewFeed(User user, String followerName, String content) {
        return Notification.create(
                NotificationLevel.INFO,
                "["+followerName+"]"+ "님이 새로운 피드를 등록했습니다.",
                content,
                NotificationType.FOLLOWER_NEW_FEED,
                user
        );
    }

    public Notification attributeAdded(User user, String attributeName, AttributeAction attributeAction) {
        return Notification.create(
                NotificationLevel.INFO,
                attributeAction.getMessage(),
                "["+attributeName+"]"+"속성을 확인해보세요",
                NotificationType.ATTRIBUTE_ADDED,
                user
        );
    }

}
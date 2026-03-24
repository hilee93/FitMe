package com.ootd.fitme.domain.notiication.service;

import com.ootd.fitme.domain.notiication.entity.Notification;
import com.ootd.fitme.domain.notiication.entity.NotificationFactory;
import com.ootd.fitme.domain.notiication.repository.NotificationRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;
    private final UserRepository userRepository;


    @Transactional
    public Notification notifyDirectMessage(UUID userId, String senderName,String message) {

        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Notification notification = notificationFactory.dm(receiver, senderName,message);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyFollowed(User user, String followerName) {
        Notification notification = notificationFactory.followed(user, followerName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyWeatherAlert(User user, String weatherAlert) {
        Notification notification = notificationFactory.weatherAlert(user, weatherAlert);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyFeedLiked(User user, String likerName) {
        Notification notification = notificationFactory.feedLiked(user, likerName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyFeedCommented(User user, String commenterName, String comment) {
        Notification notification = notificationFactory.feedCommented(user, commenterName,comment);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyFollowerNewFeed(User user, String writerName, String feedName) {
        Notification notification = notificationFactory.followerNewFeed(user, writerName,feedName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyAttributeUpdated(User user, String attributeName) {
        Notification notification = notificationFactory.attributeUpdated(user, attributeName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyAttributeAdded(User user, String attributeName) {
        Notification notification = notificationFactory.attributeAdded(user, attributeName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyAttributeDeleted(User user, String attributeName) {
        Notification notification = notificationFactory.attributeDeleted(user, attributeName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void delete(UUID notificationId) {

        notificationRepository.deleteById(notificationId);
    }
}
